package com.ywt.chat.service.adapter;

import com.ywt.chat.domain.entity.Contact;
import com.ywt.chat.domain.vo.Resp.ChatMessageReadResp;

import java.util.List;
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
}
