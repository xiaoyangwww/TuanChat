package com.ywt.chat.service;

import com.ywt.chat.domain.vo.Req.ChatMessageReq;
import com.ywt.chat.domain.vo.Resp.ChatMessageResp;

public interface ChatService {

    /**
     * 发送消息
     * @param request
     * @param uid
     * @return
     */
    Long sendMsg(ChatMessageReq request, Long uid);

    /**
     * 返回完整消息格式
     * @param msgId
     * @param uid
     * @return
     */
    ChatMessageResp getMsgResp(Long msgId, Long uid);
}
