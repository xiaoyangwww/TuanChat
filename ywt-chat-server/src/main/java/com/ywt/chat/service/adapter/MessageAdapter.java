package com.ywt.chat.service.adapter;

import com.ywt.chat.domain.entity.Message;
import com.ywt.chat.domain.enums.MessageStatusEnum;
import com.ywt.chat.domain.vo.Req.ChatMessageReq;

/**
 * 功能描述
 * 消息适配器
 * @author: scott
 * @date: 2024年05月03日 11:59
 */
public class MessageAdapter {


    public static Message buildMsgSave(ChatMessageReq request, Long uid) {
        return Message.builder()
                .fromUid(uid)
                .roomId(request.getRoomId())
                .type(request.getMsgType())
                .status(MessageStatusEnum.NORMAL.getStatus())
                .build();
    }
}
