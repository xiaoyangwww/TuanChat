package com.ywt.chat.service.adapter;

import com.ywt.chat.domain.entity.Contact;
import com.ywt.chat.domain.entity.Room;
import com.ywt.chat.domain.entity.RoomGroup;
import com.ywt.chat.domain.enums.HotFlagEnum;
import com.ywt.chat.domain.enums.MessageTypeEnum;
import com.ywt.chat.domain.enums.RoomTypeEnum;
import com.ywt.chat.domain.vo.Req.ChatMessageReq;
import com.ywt.chat.domain.vo.Resp.ChatMessageReadResp;
import com.ywt.user.domain.entity.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月13日 17:49
 */
public class RoomAdapter {
    public static List<ChatMessageReadResp> buildReadResp(List<Contact> list) {
        return list.stream().map(contact -> {
            ChatMessageReadResp chatMessageReadResp = new ChatMessageReadResp();
            chatMessageReadResp.setUid(contact.getUid());
            return chatMessageReadResp;
        }).collect(Collectors.toList());
    }

    public static Room buildRoom(RoomTypeEnum typeEnum) {
        return Room.builder()
                .hotFlag(HotFlagEnum.NOT.getType())
                .type(typeEnum.getType())
                .build();

    }

    public static RoomGroup buildRoomGroup(User user, Long roomId) {
        return RoomGroup.builder()
                .roomId(roomId)
                .name(user.getName() + "的群组")
                .avatar(user.getAvatar())
                .build();
    }

    public static ChatMessageReq buildGroupAddMessage(RoomGroup roomGroup, User inviter, Map<Long, User> member) {
        ChatMessageReq chatMessageReq = new ChatMessageReq();
        chatMessageReq.setRoomId(roomGroup.getRoomId());
        chatMessageReq.setMsgType(MessageTypeEnum.SYSTEM.getType());
        StringBuilder sb = new StringBuilder();
        sb.append("\"")
                .append(inviter.getName())
                .append("\"")
                .append("邀请")
                .append(member.values().stream().map(u -> "\"" + u.getName() + "\"").collect(Collectors.joining(",")))
                .append("加入群聊");
        chatMessageReq.setBody(sb.toString());
        return chatMessageReq;
    }
}
