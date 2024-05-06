package com.ywt.chat.service;

import com.ywt.chat.domain.entity.Message;
import com.ywt.chat.domain.vo.Req.ChatMessageReq;
import com.ywt.chat.domain.vo.Resp.ChatMessageResp;
import com.ywt.common.domain.vo.Req.ChatMessagePageReq;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;

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

    /**
     * 返回完整消息格式
     * @param uid
     * @return
     */
    ChatMessageResp getMsgResp(Message message, Long uid);

    /**
     * 获取消息列表
     * @param request
     * @param uid
     * @return
     */
    CursorPageBaseResp<ChatMessageResp> getMsgPage(ChatMessagePageReq request, Long uid);
}
