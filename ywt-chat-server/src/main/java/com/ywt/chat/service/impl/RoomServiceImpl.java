package com.ywt.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjectUtil;
import com.ywt.chat.dao.*;
import com.ywt.chat.domain.dto.RoomBaseInfo;
import com.ywt.chat.domain.entity.*;
import com.ywt.chat.domain.enums.GroupRoleAPPEnum;
import com.ywt.chat.domain.enums.HotFlagEnum;
import com.ywt.chat.domain.enums.RoomTypeEnum;
import com.ywt.chat.domain.vo.Resp.ChatRoomResp;
import com.ywt.chat.service.RoomService;
import com.ywt.chat.service.adapter.ChatAdapter;
import com.ywt.chat.service.adapter.RoomAdapter;
import com.ywt.chat.service.cache.*;
import com.ywt.chat.service.strategy.mark.AbstractMsgMarkStrategy;
import com.ywt.chat.service.strategy.msg.AbstractMsgHandler;
import com.ywt.chat.service.strategy.msg.MsgHandlerFactory;
import com.ywt.common.domain.enums.NormalOrNoEnum;
import com.ywt.common.domain.vo.Req.CursorPageBaseReq;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;
import com.ywt.common.utils.AssertUtil;
import com.ywt.user.cache.UserInfoCache;
import com.ywt.user.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年05月06日 16:51
 */
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private RoomFriendDao roomFriendDao;

    @Autowired
    private ContactDao contactDao;

    @Autowired
    private HotRoomCache hotRoomCache;

    @Autowired
    private RoomCache roomCache;

    @Autowired
    private RoomGroupCache roomGroupCache;

    @Autowired
    private RoomFriendCache roomFriendCache;

    @Autowired
    private UserInfoCache userInfoCache;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    @Lazy
    private RoomService roomService;

    @Autowired
    private GroupMemberDao groupMemberDao;

    @Autowired
    private RoomGroupDao roomGroupDao;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoomFriend createRoomFriend(List<Long> uidList) {
        AssertUtil.isNotEmpty(uidList, "房间创建失败，好友数量不对");
        AssertUtil.equal(uidList.size(), 2, "房间创建失败，好友数量不对");
        // 获取聊天室的幂等key
        String key = ChatAdapter.generateRoomKey(uidList);
        RoomFriend roomFriend = roomFriendDao.getByRoomKey(key);
        if (ObjectUtil.isNotNull(roomFriend)) {
            // 如果存在房间就恢复，适用于恢复好友场景
            restoreRoomIfNeed(roomFriend);
        } else {
            Room room = createRoom(RoomTypeEnum.FRIEND);
            roomFriend = createRoomFriend(room.getId(), uidList);
        }
        return roomFriend;
    }

    @Override
    public void disableChat(List<Long> uidList) {
        AssertUtil.isNotEmpty(uidList, "房间禁用失败，好友数量不对");
        AssertUtil.equal(uidList.size(), 2, "房间禁用失败，好友数量不对");
        RoomFriend roomFriend = roomFriendDao.getByRoomKey(ChatAdapter.generateRoomKey(uidList));
        if (ObjectUtil.isNull(roomFriend)) {
            return;
        }
        // 禁用房间
        roomFriendDao.disableRoom(roomFriend.getId());
    }

    @Override
    public CursorPageBaseResp<ChatRoomResp> getContactPage(CursorPageBaseReq request, Long uid) {
        // 查出用户要展示的会话列表
        CursorPageBaseResp<Long> page;
        // 用户登录
        if (ObjectUtil.isNotNull(uid)) {
            Double hotEnd = getCursorOrNull(request.getCursor());
            Double hotStart = null;
            // 用户的基础会话
            CursorPageBaseResp<Contact> userContactPage = contactDao.getContactPage(uid, request);
            List<Long> baseRoomIds = userContactPage.getList().stream().map(Contact::getRoomId).collect(Collectors.toList());
            if (!userContactPage.getIsLast()) {
                hotStart = getCursorOrNull(userContactPage.getCursor());
            }
            // 热门房间
            Set<ZSetOperations.TypedTuple<String>> hotContact = hotRoomCache.getRoomRange(hotStart, hotEnd);
            List<Long> hotRoomIds = hotContact.stream().map(ZSetOperations.TypedTuple::getValue).filter(Objects::nonNull).map(Long::parseLong).collect(Collectors.toList());
            baseRoomIds.addAll(hotRoomIds);
            // 基础会话和热门房间合并
            page = CursorPageBaseResp.init(userContactPage, baseRoomIds);
        } else {
            // 用户未登录，只查全局房间
            CursorPageBaseResp<Pair<Long, Double>> roomCursorPage = hotRoomCache.getRoomCursorPage(request);
            List<Long> hotRoomIds = roomCursorPage.getList().stream().map(Pair::getKey).collect(Collectors.toList());
            page = CursorPageBaseResp.init(roomCursorPage, hotRoomIds);
        }
        if (CollectionUtil.isEmpty(page.getList())) {
            return CursorPageBaseResp.empty();
        }
        // 最后组装会话信息（名称，头像，未读数等）
        List<ChatRoomResp> result = buildContactResp(uid, page.getList());
        return CursorPageBaseResp.init(page, result);
    }

    @Override
    public ChatRoomResp getContactDetail(Long roomId, long uid) {
        Room room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间号有误");
        return buildContactResp(uid, Collections.singletonList(roomId)).get(0);
    }

    @Override
    public ChatRoomResp getContactDetailByFriend(Long uid, Long friendUid) {
        RoomFriend friendRoom = roomService.getFriendRoom(uid, friendUid);
        AssertUtil.isNotEmpty(friendRoom, "他不是您的好友");
        return buildContactResp(uid, Collections.singletonList(friendRoom.getRoomId())).get(0);
    }

    @Override
    public RoomFriend getFriendRoom(Long uid, Long friendUid) {
        String key = ChatAdapter.generateRoomKey(Arrays.asList(uid, friendUid));
        return roomFriendDao.getByRoomKey(key);
    }

    @Override
    public RoomGroup createGroupRoom(Long uid) {
        List<GroupMember> selfGroup = groupMemberDao.getSelfGroup(uid);
        AssertUtil.isEmpty(selfGroup, "每个人只能创建一个群");
        User user = userInfoCache.get(uid);
        // 创建房间
        Room room = RoomAdapter.buildRoom(RoomTypeEnum.GROUP);
        roomDao.save(room);
        // 创建群聊
        RoomGroup roomGroup = RoomAdapter.buildRoomGroup(user,room.getId());
        roomGroupDao.save(roomGroup);
        // 插入群主
        GroupMember leader = GroupMember.builder()
                .groupId(roomGroup.getId())
                .role(GroupRoleAPPEnum.LEADER.getType())
                .uid(uid)
                .build();
        groupMemberDao.save(leader);
        return roomGroup;
    }

    private List<ChatRoomResp> buildContactResp(Long uid, List<Long> roomList) {
        // 表情和头像
        Map<Long, RoomBaseInfo> roomBaseInfoMap = getRoomBaseInfoMap(roomList, uid);
        // 最后一条消息
        List<Long> msgIds = roomBaseInfoMap.values().stream().map(RoomBaseInfo::getLastMsgId).collect(Collectors.toList());
        List<Message> messages = CollectionUtil.isEmpty(msgIds) ? new ArrayList<>() : messageDao.listByIds(msgIds);
        Map<Long, Message> msgMap = messages.stream().collect(Collectors.toMap(Message::getId, Function.identity()));
        // 发最后一条消息的用户
        Map<Long, User> lastMsgUidMap = userInfoCache.getBatch(messages.stream().map(Message::getFromUid).collect(Collectors.toList()));
        // 消息未读数
        Map<Long, Integer> unReadCountMap = getUnReadCountMap(uid, roomList);
        return roomBaseInfoMap.values().stream().map(roomBaseInfo -> {
            ChatRoomResp chatRoomResp = new ChatRoomResp();
            BeanUtil.copyProperties(roomBaseInfo, chatRoomResp, true);
            chatRoomResp.setHot_Flag(roomBaseInfo.getHotFlag());
            Message message = msgMap.get(roomBaseInfo.getLastMsgId());
            if (ObjectUtil.isNotNull(message)) {
                AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategyNoNull(message.getType());
                chatRoomResp.setText(lastMsgUidMap.get(message.getFromUid()).getName() + ":" + msgHandler.showContactMsg(message));
            }
            chatRoomResp.setUnreadCount(unReadCountMap.getOrDefault(roomBaseInfo.getRoomId(), 0));
            return chatRoomResp;
        }).sorted(Comparator.comparing(ChatRoomResp::getActiveTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 获取未读数
     */
    private Map<Long, Integer> getUnReadCountMap(Long uid, List<Long> roomList) {
        if (Objects.isNull(uid)) {
            return new HashMap<>();
        }
        List<Contact> contactList = contactDao.getByRoomIds(uid, roomList);
        // 并行流
        return contactList.parallelStream()
                .map(contact -> Pair.of(contact.getRoomId(), messageDao.getUnReadCountMap(contact.getRoomId(), contact.getReadTime())))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    private Map<Long, RoomBaseInfo> getRoomBaseInfoMap(List<Long> roomList, Long uid) {
        Map<Long, Room> roomMap = roomCache.getBatch(roomList);
        // 房间根据好友和群组类型分组 下面的Long都代表roomId
        Map<Integer, List<Long>> groupRoomIdMap = roomMap.values().stream().collect(Collectors.groupingBy(Room::getType, Collectors.mapping(Room::getId, Collectors.toList())));
        // 群聊
        List<Long> groupRoomIds = groupRoomIdMap.get(RoomTypeEnum.GROUP.getType());
        Map<Long, RoomGroup> groupRoomMap = roomGroupCache.getBatch(groupRoomIds);
        // 单聊获取好友信息
        List<Long> friendRoomIds = groupRoomIdMap.get(RoomTypeEnum.FRIEND.getType());
        Map<Long, User> friendInfoMap = getFriendInfoMap(friendRoomIds, uid);
        return roomMap.values().stream().map(room -> {
            RoomBaseInfo roomBaseInfo = new RoomBaseInfo();
            roomBaseInfo.setRoomId(room.getId());
            roomBaseInfo.setType(room.getType());
            roomBaseInfo.setHotFlag(room.getHotFlag());
            roomBaseInfo.setLastMsgId(room.getLastMsgId());
            roomBaseInfo.setActiveTime(room.getActiveTime());
            if (RoomTypeEnum.of(room.getType()) == RoomTypeEnum.GROUP) {
                RoomGroup roomGroup = groupRoomMap.get(room.getId());
                roomBaseInfo.setName(roomGroup.getName());
                roomBaseInfo.setAvatar(roomGroup.getAvatar());
            } else if (RoomTypeEnum.of(room.getType()) == RoomTypeEnum.FRIEND) {
                User user = friendInfoMap.get(room.getId());
                roomBaseInfo.setName(user.getName());
                roomBaseInfo.setAvatar(user.getAvatar());
            }
            return roomBaseInfo;
        }).collect(Collectors.toMap(RoomBaseInfo::getRoomId, Function.identity()));
    }

    private Map<Long, User> getFriendInfoMap(List<Long> roomList, Long uid) {
        if (CollectionUtil.isEmpty(roomList)) {
            return new HashMap<>();
        }
        Map<Long, RoomFriend> roomFriendMap = roomFriendCache.getBatch(roomList);
        Set<Long> friendUidSet = ChatAdapter.getFriendUidSet(roomFriendMap.values(), uid);
        Map<Long, User> userBatch = userInfoCache.getBatch(new ArrayList<>(friendUidSet));
        return roomFriendMap.values()
                .stream()
                .collect(Collectors.toMap(RoomFriend::getRoomId, roomFriend -> {
                    Long friendUid = ChatAdapter.getFriendUid(roomFriend, uid);
                    return userBatch.get(friendUid);
                }));

    }

    private Double getCursorOrNull(String cursor) {
        return Optional.ofNullable(cursor).map(Double::parseDouble).orElse(null);
    }

    private void restoreRoomIfNeed(RoomFriend roomFriend) {
        if (roomFriend.getStatus().equals(NormalOrNoEnum.NOT_NORMAL.getStatus())) {
            roomFriendDao.restoreRoom(roomFriend.getId());
        }
    }

    public RoomFriend createRoomFriend(Long roomId, List<Long> uidList) {
        RoomFriend roomFriend = ChatAdapter.buildRoomFriend(roomId, uidList);
        roomFriendDao.save(roomFriend);
        return roomFriend;
    }


    private Room createRoom(RoomTypeEnum roomTypeEnum) {
        Room room = ChatAdapter.buildRoom(roomTypeEnum);
        roomDao.save(room);
        return room;

    }
}
