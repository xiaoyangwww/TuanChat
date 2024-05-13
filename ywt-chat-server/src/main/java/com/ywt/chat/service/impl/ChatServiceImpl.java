package com.ywt.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ywt.chat.dao.*;
import com.ywt.chat.domain.dto.MsgReadInfoDTO;
import com.ywt.chat.domain.entity.*;
import com.ywt.chat.domain.enums.MessageMarkActTypeEnum;
import com.ywt.chat.domain.enums.MessageReadEnum;
import com.ywt.chat.domain.enums.MessageTypeEnum;
import com.ywt.chat.domain.vo.Req.*;
import com.ywt.chat.domain.vo.Req.msg.ChatMessageMarkReq;
import com.ywt.chat.domain.vo.Resp.ChatMessageReadResp;
import com.ywt.chat.domain.vo.Resp.ChatMessageResp;
import com.ywt.chat.mapper.RoomMapper;
import com.ywt.chat.service.ChatService;
import com.ywt.chat.service.ContactService;
import com.ywt.chat.service.adapter.MessageAdapter;
import com.ywt.chat.service.adapter.RoomAdapter;
import com.ywt.chat.service.cache.RoomCache;
import com.ywt.chat.service.cache.RoomFriendCache;
import com.ywt.chat.service.cache.RoomGroupCache;
import com.ywt.chat.service.strategy.mark.AbstractMsgMarkStrategy;
import com.ywt.chat.service.strategy.mark.MsgMarkFactory;
import com.ywt.chat.service.strategy.msg.AbstractMsgHandler;
import com.ywt.chat.service.strategy.msg.MsgHandlerFactory;
import com.ywt.chat.service.strategy.msg.RecallMsgHandler;
import com.ywt.common.annotation.RedissonLock;
import com.ywt.common.domain.enums.NormalOrNoEnum;
import com.ywt.common.domain.vo.Req.ChatMessagePageReq;
import com.ywt.common.domain.vo.Req.CursorPageBaseReq;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;
import com.ywt.common.event.MessageSendEvent;
import com.ywt.common.utils.AssertUtil;
import com.ywt.user.domain.enums.RoleEnum;
import com.ywt.user.service.RoleService;
import org.jsoup.helper.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年05月03日 10:05
 */
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private RoomCache roomCache;

    @Autowired
    private RoomFriendCache roomFriendCache;

    @Autowired
    private RoomGroupCache roomGroupCache;

    @Autowired
    private RoomGroupDao roomGroupDao;

    @Autowired
    private GroupMemberDao groupMemberDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private ContactDao contactDao;

    @Autowired
    private MessageMarkDao messageMarkDao;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RecallMsgHandler recallMsgHandler;

    @Autowired
    private ContactService contactService;


    /**
     * 发送消息
     */
    @Override
    @Transactional
    public Long sendMsg(ChatMessageReq request, Long uid) {
        // 发送信息的房间检查
        check(request, uid);
        // 根据消息类型取出响应的处理器
        AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategyNoNull(request.getMsgType());
        Long msgId = msgHandler.checkAndSaveMsg(request, uid);
        //发布消息发送事件
        applicationEventPublisher.publishEvent(new MessageSendEvent(this, msgId));
        return msgId;
    }

    private void check(ChatMessageReq request, Long uid) {
        Long roomId = request.getRoomId();
        Room room = roomCache.get(roomId);
        // 判断是否是热点群聊
        if (room.isHotRoom()) {
            return;
        }
        // 判断是否是单聊
        if (room.isRoomFriend()) {
            RoomFriend roomFriend = roomFriendCache.get(roomId);
            // 校验房间是否被禁用
            AssertUtil.equal(NormalOrNoEnum.NOT_NORMAL.getStatus(), roomFriend.getStatus(), "您已经被对方拉黑");
            // 校验uid是否在房间内
            AssertUtil.isTrue(uid.equals(roomFriend.getUid1()) || uid.equals(roomFriend.getUid2()), "您已经被对方拉黑");
        }
        // 判断是否是群聊
        if (room.isRoomGroup()) {
            RoomGroup roomGroup = roomGroupCache.get(roomId);
            // 判断用户是否是群成员
            GroupMember groupMember = groupMemberDao.getMember(roomGroup.getId(), uid);
            AssertUtil.isNotEmpty(groupMember, "您已经被移除该群");
        }
    }

    @Override
    public ChatMessageResp getMsgResp(Long msgId, Long uid) {
        Message message = messageDao.getById(msgId);
        return getMsgResp(message, uid);
    }

    @Override
    public ChatMessageResp getMsgResp(Message message, Long receiveUid) {
        return CollUtil.getFirst(getMsgRespBatch(Collections.singletonList(message), receiveUid));
    }

    @Override
    public CursorPageBaseResp<ChatMessageResp> getMsgPage(ChatMessagePageReq request, Long uid) {
        //获取这个用户对这个会话的最后一条信息id，来限制被踢出的人能看见的最大一条消息
        Long lastMsgId = getLastMsgId(request.getRoomId(), uid);
        // 游标翻页
        CursorPageBaseResp<Message> cursorPage = messageDao.getCursorPage(request.getRoomId(), request, lastMsgId);
        if (cursorPage.isEmpty()) {
            return CursorPageBaseResp.empty();
        }
        return CursorPageBaseResp.init(cursorPage, getMsgRespBatch(cursorPage.getList(), uid));
    }

    @Override
    public void recallMsg(Long uid, ChatMessageBaseReq request) {
        Message message = messageDao.getById(request.getMsgId());
        //校验能不能执行撤回
        checkRecall(message, uid);
        //执行消息撤回
        recallMsgHandler.recall(uid, message);
    }

    @Override
    @RedissonLock(key = "#uid")
    public void setMsgMark(Long uid, ChatMessageMarkReq request) {
        AbstractMsgMarkStrategy strategy = MsgMarkFactory.getStrategyNoNull(request.getMarkType());
        Integer actType = request.getActType();
        switch (MessageMarkActTypeEnum.of(actType)) {
            case MARK:
                strategy.mark(uid, request.getMsgId());
                break;
            case UN_MARK:
                strategy.unMark(uid, request.getMsgId());
                break;
        }
    }

    @Override
    @RedissonLock(key = "#uid")
    public void msgRead(Long uid, ChatMessageMemberReq request) {
        Contact contact = contactDao.get(request.getRoomId(), uid);
        Contact saveOrUpdate = new Contact();
        if (ObjectUtil.isNotNull(contact)) {
            saveOrUpdate.setId(contact.getId());
            saveOrUpdate.setReadTime(new Date());
        } else {
            saveOrUpdate.setReadTime(new Date());
            saveOrUpdate.setUid(uid);
            saveOrUpdate.setRoomId(request.getRoomId());
        }
        contactDao.saveOrUpdate(saveOrUpdate);
    }

    @Override
    public CursorPageBaseResp<ChatMessageReadResp> getReadPage(Long uid, ChatMessageReadReq request) {
        Message message = messageDao.getById(request.getMsgId());
        Long searchType = request.getSearchType();
        CursorPageBaseResp<Contact> page;
        if (MessageReadEnum.READ.getType().equals(searchType)) {
            page = contactDao.getReadPage(request, message);// 获取信息已读列表
        } else {
            page = contactDao.getUnReadPage(request, message); // 获取信息未读列表
        }
        if (CollectionUtil.isEmpty(page.getList())) {
            return CursorPageBaseResp.empty();
        }
        return CursorPageBaseResp.init(page, RoomAdapter.buildReadResp(page.getList()));
    }

    @Override
    public Collection<MsgReadInfoDTO> getMsgReadInfo(Long uid, ChatMessageReadInfoReq request) {
        List<Message> messages = messageDao.listByIds(request.getMsgIds());
        messages.forEach(message -> {
            AssertUtil.equal(uid, message.getFromUid(), "只能查询自己发送的消息");
        });
        return contactService.getMsgReadInfo(messages);
    }

    private void checkRecall(Message message, Long uid) {
        AssertUtil.isNotEmpty(message, "消息有误");
        AssertUtil.notEqual(message.getType(), MessageTypeEnum.RECALL.getType(), "消息无法撤回");
        boolean hasPower = roleService.hasPower(uid, RoleEnum.ADMIN);
        if (hasPower) {
            return;
        }
        AssertUtil.equal(uid, message.getFromUid(), "您没有权限");
        long between = DateUtil.between(message.getCreateTime(), new Date(), DateUnit.MINUTE);
        AssertUtil.isTrue(between <= 2, "覆水难收，超过2分钟的消息不能撤回哦~~");
    }

    private List<ChatMessageResp> getMsgRespBatch(List<Message> messages, Long uid) {
        if (CollectionUtil.isEmpty(messages)) {
            return new ArrayList<>();
        }
        List<MessageMark> messageMarkList = messageMarkDao.getValidMarkByMsgIdBatch(messages.stream().map(Message::getId).collect(Collectors.toList()));
        return MessageAdapter.buildMsgResp(messages, messageMarkList, uid);

    }

    private Long getLastMsgId(Long roomId, Long uid) {
        Room room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间号有误");
        if (room.isHotRoom()) {
            return null;
        }
        AssertUtil.isNotEmpty(uid, "请先登录");
        Contact contact = contactDao.get(roomId, uid);
        return contact.getLastMsgId();
    }
}
