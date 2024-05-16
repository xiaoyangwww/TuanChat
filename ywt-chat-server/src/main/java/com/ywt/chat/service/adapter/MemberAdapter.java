package com.ywt.chat.service.adapter;

import com.ywt.chat.domain.entity.GroupMember;
import com.ywt.chat.domain.enums.GroupRoleAPPEnum;
import com.ywt.chat.domain.vo.Resp.ChatMemberListResp;
import com.ywt.user.domain.entity.User;
import com.ywt.websocket.domain.enums.WSRespTypeEnum;
import com.ywt.websocket.domain.vo.message.ChatMemberResp;
import com.ywt.websocket.domain.vo.message.WSMemberChange;
import com.ywt.websocket.domain.vo.resp.WSBaseResp;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月14日 17:44
 */
public class MemberAdapter {

    public static List<ChatMemberListResp> buildMemberList(List<User> members) {
        return members.stream().map(user -> {
            ChatMemberListResp chatMemberListResp = new ChatMemberListResp();
            chatMemberListResp.setUid(user.getId());
            chatMemberListResp.setName(user.getName());
            chatMemberListResp.setAvatar(user.getAvatar());
            return chatMemberListResp;
        }).collect(Collectors.toList());
    }

    public static WSBaseResp<WSMemberChange> buildMemberRemoveWS(Long roomId, Long uid) {
        WSBaseResp<WSMemberChange> wsMemberChangeWSBaseResp = new WSBaseResp<>();
        wsMemberChangeWSBaseResp.setType(WSRespTypeEnum.MEMBER_CHANGE.getType());
        WSMemberChange wsMemberChange = new WSMemberChange();
        wsMemberChange.setUid(uid);
        wsMemberChange.setRoomId(roomId);
        wsMemberChange.setChangeType(WSMemberChange.CHANGE_TYPE_REMOVE);
        wsMemberChangeWSBaseResp.setData(wsMemberChange);
        return wsMemberChangeWSBaseResp;
    }

    public static WSBaseResp<WSMemberChange> buildMemberAddWS(Long roomId, User user) {
        WSBaseResp<WSMemberChange> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WSRespTypeEnum.MEMBER_CHANGE.getType());
        WSMemberChange wsMemberChange = new WSMemberChange();
        wsMemberChange.setActiveStatus(user.getActiveStatus());
        wsMemberChange.setLastOptTime(user.getLastOptTime());
        wsMemberChange.setUid(user.getId());
        wsMemberChange.setRoomId(roomId);
        wsMemberChange.setChangeType(WSMemberChange.CHANGE_TYPE_ADD);
        wsBaseResp.setData(wsMemberChange);
        return wsBaseResp;
    }

    public static List<GroupMember> buildMemberAdd(Long groupId, List<Long> waitAddUidList) {
        return waitAddUidList.stream().map(uid -> GroupMember.builder()
                .groupId(groupId)
                .role(GroupRoleAPPEnum.MEMBER.getType())
                .uid(uid)
                .build()).collect(Collectors.toList());
    }

    public static List<ChatMemberResp> buildMember(List<User> list) {
        return list.stream().map(a -> {
            ChatMemberResp resp = new ChatMemberResp();
            resp.setActiveStatus(a.getActiveStatus());
            resp.setLastOptTime(a.getLastOptTime());
            resp.setUid(a.getId());
            return resp;
        }).collect(Collectors.toList());
    }
}
