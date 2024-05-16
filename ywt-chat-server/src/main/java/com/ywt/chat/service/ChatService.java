package com.ywt.chat.service;

import com.ywt.chat.domain.dto.MsgReadInfoDTO;
import com.ywt.chat.domain.entity.Message;
import com.ywt.chat.domain.vo.Req.*;
import com.ywt.chat.domain.vo.Req.member.MemberReq;
import com.ywt.chat.domain.vo.Req.msg.ChatMessageMarkReq;
import com.ywt.chat.domain.vo.Resp.ChatMemberStatisticResp;
import com.ywt.chat.domain.vo.Resp.ChatMessageReadResp;
import com.ywt.chat.domain.vo.Resp.ChatMessageResp;
import com.ywt.common.domain.vo.Req.ChatMessagePageReq;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;
import com.ywt.websocket.domain.vo.message.ChatMemberResp;

import java.util.Collection;
import java.util.List;

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

    /**
     * 撤回消息
     * @param uid
     * @param request
     */
    void recallMsg(Long uid, ChatMessageBaseReq request);

    /**
     * 消息标记
     * @param uid
     * @param request
     */
    void setMsgMark(Long uid, ChatMessageMarkReq request);

    /**
     * 消息阅读上报(更新用户在会话下的阅读时间)
     * @param uid
     * @param request
     */
    void msgRead(Long uid, ChatMessageMemberReq request);

    /**
     * 消息的已读未读列表
     * @param uid
     * @param request
     * @return
     */
    CursorPageBaseResp<ChatMessageReadResp> getReadPage(Long uid, ChatMessageReadReq request);

    /**
     * 获取消息的已读未读总数
     * @param uid
     * @param request
     * @return
     */
    Collection<MsgReadInfoDTO> getMsgReadInfo(Long uid, ChatMessageReadInfoReq request);

    /**
     * 获取上线人数
     * @return
     */
    ChatMemberStatisticResp getMemberStatistic();

    /**
     * 根据群成员获取上线，下线列表
     * @return
     */
    CursorPageBaseResp<ChatMemberResp> getMemberPage(List<Long> memberUidList, MemberReq request);
}
