package com.ywt.chat.service.impl;

import com.ywt.chat.dao.GroupMemberDao;
import com.ywt.chat.dao.MessageDao;
import com.ywt.chat.dao.RoomGroupDao;
import com.ywt.chat.domain.entity.*;
import com.ywt.chat.domain.vo.Req.ChatMessageReq;
import com.ywt.chat.domain.vo.Resp.ChatMessageResp;
import com.ywt.chat.mapper.RoomMapper;
import com.ywt.chat.service.ChatService;
import com.ywt.chat.service.cache.RoomCache;
import com.ywt.chat.service.cache.RoomFriendCache;
import com.ywt.chat.service.cache.RoomGroupCache;
import com.ywt.chat.service.strategy.msg.AbstractMsgHandler;
import com.ywt.chat.service.strategy.msg.MsgHandlerFactory;
import com.ywt.common.domain.enums.NormalOrNoEnum;
import com.ywt.common.event.MessageSendEvent;
import com.ywt.common.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 功能描述
 *
 * @author: scott
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

    /**
     * 发送消息
     */
    @Override
    @Transactional
    public Long sendMsg(ChatMessageReq request, Long uid) {
        // 发送信息的房间检查
        check(request,uid);
        // 根据消息类型取出响应的处理器
        AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategyNoNull(request.getMsgType());
        Long msgId = msgHandler.checkAndSaveMsg(request, uid);
        //发布消息发送事件
        applicationEventPublisher.publishEvent(new MessageSendEvent(this,msgId));
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
            AssertUtil.equal(NormalOrNoEnum.NOT_NORMAL.getStatus(),roomFriend.getStatus(),"您已经被对方拉黑");
            // 校验uid是否在房间内
            AssertUtil.isTrue(uid.equals(roomFriend.getUid1()) || uid.equals(roomFriend.getUid2()),"您已经被对方拉黑");
        }
        // 判断是否是群聊
        if (room.isRoomGroup()) {
            RoomGroup roomGroup = roomGroupCache.get(roomId);
            // 判断用户是否是群成员
            GroupMember groupMember = groupMemberDao.getMember(roomGroup.getId(),uid);
            AssertUtil.isNotEmpty(groupMember, "您已经被移除该群");
        }
    }

    @Override
    public ChatMessageResp getMsgResp(Long msgId, Long uid) {
        Message message = messageDao.getById(msgId);
        ChatMessageResp chatMessageResp = new ChatMessageResp();
        ChatMessageResp.UserInfo userInfo = new ChatMessageResp.UserInfo();
        userInfo.setUid(uid);
        chatMessageResp.setFromUser(userInfo);
        ChatMessageResp.Message message1 = new ChatMessageResp.Message();
        message1.setId(msgId);
        message1.setRoomId(message.getRoomId());
        message1.setSendTime(message.getCreateTime());
        message1.setType(message.getType());
        message1.setBody(message.getExtra());
        message1.setMessageMark(null);
        chatMessageResp.setMessage(message1);
        return chatMessageResp;
    }
}
