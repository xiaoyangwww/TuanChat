package com.ywt.common.event.listener;

import com.ywt.chat.domain.entity.GroupMember;
import com.ywt.chat.domain.entity.RoomGroup;
import com.ywt.chat.domain.vo.Req.ChatMessageReq;
import com.ywt.chat.service.ChatService;
import com.ywt.chat.service.PushService;
import com.ywt.chat.service.adapter.MemberAdapter;
import com.ywt.chat.service.adapter.RoomAdapter;
import com.ywt.chat.service.cache.GroupMemberCache;
import com.ywt.common.event.GroupMemberAddEvent;
import com.ywt.user.cache.UserInfoCache;
import com.ywt.user.dao.UserDao;
import com.ywt.user.domain.entity.User;
import com.ywt.websocket.domain.vo.message.WSMemberChange;
import com.ywt.websocket.domain.vo.resp.WSBaseResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月14日 22:38
 */
@Component
public class GroupMemberListener {

    @Autowired
    private UserInfoCache userInfoCache;

    @Autowired
    private ChatService chatService;

    @Autowired
    private GroupMemberCache groupMemberCache;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PushService pushService;

    /**
     * 创建群聊发送第一条系统消息
     * @param event
     */
    @Async
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendAddMsg(GroupMemberAddEvent event) {
        List<GroupMember> memberList = event.getMemberList();
        RoomGroup roomGroup = event.getRoomGroup();
        Long inviteUid = event.getInviteUid();
        User user = userInfoCache.get(inviteUid);
        List<Long> uidList = memberList.stream().map(GroupMember::getUid).collect(Collectors.toList());
        ChatMessageReq chatMessageReq = RoomAdapter.buildGroupAddMessage(roomGroup, user, userInfoCache.getBatch(uidList));
        chatService.sendMsg(chatMessageReq, User.UID_SYSTEM);
    }

    /**
     * 给被拉入群聊的成员发送通知
     * @param event
     */
    @Async
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendChangePush(GroupMemberAddEvent event) {
        List<GroupMember> memberList = event.getMemberList();
        RoomGroup roomGroup = event.getRoomGroup();
        List<Long> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
        List<Long> uidList = memberList.stream().map(GroupMember::getUid).collect(Collectors.toList());
        List<User> users = userDao.listByIds(uidList);
        users.forEach(user -> {
            WSBaseResp<WSMemberChange> ws = MemberAdapter.buildMemberAddWS(roomGroup.getRoomId(), user);
            pushService.sendPushMsg(ws, memberUidList);
        });
        //移除缓存
        groupMemberCache.evictMemberUidList(roomGroup.getRoomId());
    }

}
