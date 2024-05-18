package com.ywt.chat.service.impl;

import com.ywt.chat.dao.GroupMemberDao;
import com.ywt.chat.domain.entity.GroupMember;
import com.ywt.chat.domain.entity.Room;
import com.ywt.chat.domain.entity.RoomGroup;
import com.ywt.chat.domain.enums.GroupRoleAPPEnum;
import com.ywt.chat.domain.enums.GroupRoleEnum;
import com.ywt.chat.domain.enums.HotFlagEnum;
import com.ywt.chat.domain.vo.Req.ChatMessageMemberReq;
import com.ywt.chat.domain.vo.Req.member.MemberAddReq;
import com.ywt.chat.domain.vo.Req.member.MemberDelReq;
import com.ywt.chat.domain.vo.Req.member.MemberReq;
import com.ywt.chat.domain.vo.Resp.ChatMemberListResp;
import com.ywt.chat.domain.vo.Resp.GroupAddReq;
import com.ywt.chat.domain.vo.Resp.MemberResp;
import com.ywt.chat.service.ChatService;
import com.ywt.chat.service.PushService;
import com.ywt.chat.service.RoomAppService;
import com.ywt.chat.service.RoomService;
import com.ywt.chat.service.adapter.MemberAdapter;
import com.ywt.chat.service.cache.GroupMemberCache;
import com.ywt.chat.service.cache.RoomCache;
import com.ywt.chat.service.cache.RoomGroupCache;
import com.ywt.common.annotation.RedissonLock;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;
import com.ywt.common.event.GroupMemberAddEvent;
import com.ywt.common.exception.GroupErrorEnum;
import com.ywt.common.utils.AssertUtil;
import com.ywt.user.cache.UserCache;
import com.ywt.user.cache.UserInfoCache;
import com.ywt.user.dao.UserDao;
import com.ywt.user.domain.entity.User;
import com.ywt.user.domain.enums.RoleEnum;
import com.ywt.user.service.RoleService;
import com.ywt.websocket.domain.vo.message.ChatMemberResp;
import com.ywt.websocket.domain.vo.message.WSMemberChange;
import com.ywt.websocket.domain.vo.resp.WSBaseResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月14日 11:08
 */
@Service
public class RoomAppServiceImpl implements RoomAppService {


    @Autowired
    private RoomCache roomCache;

    @Autowired
    private RoomGroupCache roomGroupCache;

    @Autowired
    private UserCache userCache;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserInfoCache userInfoCache;


    @Autowired
    private GroupMemberDao groupMemberDao;

    @Autowired
    private GroupMemberCache groupMemberCache;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PushService pushService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private ChatService chatService;


    @Override
    public MemberResp getGroupDetail(Long uid, long roomId) {
        RoomGroup roomGroup = roomGroupCache.get(roomId);
        AssertUtil.isNotEmpty(roomGroup, "roomId有误");
        Room room = roomCache.get(roomId);
        Long onlineNum;
        if (room.isHotRoom()) {
            // 热点群从redis取人数
            onlineNum = userCache.getOnlineCount();
        } else {
            // 群成员
            List<Long> members = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
            onlineNum = userDao.getOnlineNum(members);
        }
        GroupRoleAPPEnum groupRole = getGroupRole(uid, roomGroup, room);
        return MemberResp.builder()
                .groupName(roomGroup.getName())
                .avatar(roomGroup.getAvatar())
                .role(groupRole.getType())
                .onlineNum(onlineNum)
                .roomId(roomId)
                .build();
    }

    @Override
    @Cacheable(cacheNames = "member", key = "'memberList.'+#request.roomId")
    public List<ChatMemberListResp> getMemberList(Long uid, ChatMessageMemberReq request) {
        Room room = roomCache.get(request.getRoomId());
        AssertUtil.isNotEmpty(room, "房间号有误");
        if (room.isHotRoom()) {
            List<User> members = userDao.getMemberList();
            return MemberAdapter.buildMemberList(members);
        } else {
            RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
            List<Long> members = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
            members = members.stream().filter(id -> !id.equals(uid)).collect(Collectors.toList());
            Map<Long, User> batch = userInfoCache.getBatch(members);
            return MemberAdapter.buildMemberList(new ArrayList<>(batch.values()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMember(Long uid, MemberDelReq request) {
        Room room = roomCache.get(request.getRoomId());
        AssertUtil.isNotEmpty(room, "房间号有误");
        RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
        AssertUtil.isNotEmpty(roomGroup, "房间号有误");
        GroupMember self = groupMemberDao.getMember(roomGroup.getId(), uid);
        AssertUtil.isNotEmpty(self, GroupErrorEnum.USER_NOT_IN_GROUP);
        // 1. 判断被移除的人是否是群主或者管理员  （群主不可以被移除，管理员只能被群主移除）
        Long removedUid = request.getUid();
        // 1.1 群主 非法操作
        AssertUtil.isFalse(groupMemberDao.isLord(roomGroup.getId(), removedUid), GroupErrorEnum.NOT_ALLOWED_FOR_REMOVE);
        // 1.2 管理员 判断是否是群主操作
        if (groupMemberDao.isManager(roomGroup.getId(), removedUid)) {
            Boolean isLord = groupMemberDao.isLord(roomGroup.getId(), uid);
            AssertUtil.isTrue(isLord, GroupErrorEnum.NOT_ALLOWED_FOR_REMOVE);
        }
        // 1.3 普通成员 判断是否有权限操作
        AssertUtil.isTrue(hasPower(self), GroupErrorEnum.NOT_ALLOWED_FOR_REMOVE);
        GroupMember member = groupMemberDao.getMember(roomGroup.getId(), removedUid);
        AssertUtil.isNotEmpty(member, "用户已经移除");
        groupMemberDao.removeById(member.getId());
        // 发送移除事件告知群成员
        List<Long> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
        WSBaseResp<WSMemberChange> ws = MemberAdapter.buildMemberRemoveWS(roomGroup.getRoomId(), member.getUid());
        pushService.sendPushMsg(ws, memberUidList);
        groupMemberCache.evictMemberUidList(room.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addGroup(Long uid, GroupAddReq request) {
        // 创建一个群聊并插入群主
        RoomGroup roomGroup = roomService.createGroupRoom(uid);
        // 批量保存群成员
        List<Long> uidList = request.getUidList();
        List<GroupMember> groupMemberList = uidList.stream()
                .distinct()
                .map(id -> GroupMember.builder()
                        .uid(id)
                        .role(GroupRoleEnum.MEMBER.getType())
                        .groupId(roomGroup.getId())
                        .build()
                ).collect(Collectors.toList());
        groupMemberDao.saveBatch(groupMemberList);
        // 发送邀请加群消息==》触发每个人的会话
        applicationEventPublisher.publishEvent(new GroupMemberAddEvent(this, roomGroup, groupMemberList, uid));
        return roomGroup.getRoomId();
    }

    @Override
    @RedissonLock(key = "#request.roomId")
    @Transactional(rollbackFor = Exception.class)
    public void addMember(Long uid, MemberAddReq request) {
        Room room = roomCache.get(request.getRoomId());
        AssertUtil.isNotEmpty(room, "房间号有误");
        AssertUtil.isFalse(isHotGroup(room), "全员群无需邀请好友");
        RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
        AssertUtil.isNotEmpty(roomGroup, "房间号有误");
        GroupMember self = groupMemberDao.getMember(roomGroup.getId(), uid);
        AssertUtil.isNotEmpty(self, "您不是群成员");
        List<Long> memberBatch = groupMemberDao.getMemberBatch(roomGroup.getId(), request.getUidList());
        Set<Long> existUid = new HashSet<>(memberBatch);
        List<Long> waitAddUidList = request.getUidList().stream().filter(a -> !existUid.contains(a)).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(waitAddUidList)) {
            return;
        }
        List<GroupMember> groupMembers = MemberAdapter.buildMemberAdd(roomGroup.getId(), waitAddUidList);
        groupMemberDao.saveBatch(groupMembers);
        applicationEventPublisher.publishEvent(new GroupMemberAddEvent(this, roomGroup, groupMembers, uid));
    }

    @Override
    public CursorPageBaseResp<ChatMemberResp> getMemberPage(MemberReq request) {
        Room room = roomCache.get(request.getRoomId());
        AssertUtil.isNotEmpty(room, "房间号有误");
        List<Long> memberUidList;
        if (isHotGroup(room)) {// 全员群展示所有用户
            memberUidList = null;
        } else {// 只展示房间内的群成员
            RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
            memberUidList = groupMemberDao.getUidListByRoomGroupId(roomGroup.getId());
        }
        return chatService.getMemberPage(memberUidList, request);
    }

    private boolean hasPower(GroupMember self) {
        return Objects.equals(self.getRole(), GroupRoleEnum.LEADER.getType())
                || Objects.equals(self.getRole(), GroupRoleEnum.MANAGER.getType())
                || roleService.hasPower(self.getUid(), RoleEnum.ADMIN);
    }

    private GroupRoleAPPEnum getGroupRole(Long uid, RoomGroup roomGroup, Room room) {
        GroupMember member = Objects.isNull(uid) ? null : groupMemberDao.getMember(roomGroup.getId(), uid);
        if (Objects.nonNull(member)) {
            return GroupRoleAPPEnum.of(member.getRole());
        } else if (isHotGroup(room)) {
            return GroupRoleAPPEnum.MEMBER;
        } else {
            // TODO 有问题
            return GroupRoleAPPEnum.REMOVE;
        }
    }

    private boolean isHotGroup(Room room) {
        return HotFlagEnum.YES.getType().equals(room.getHotFlag());
    }
}
