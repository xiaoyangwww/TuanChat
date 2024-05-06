package com.ywt.websocket.service.adapter;

import com.ywt.chat.domain.vo.Resp.ChatMessageResp;
import com.ywt.common.domain.enums.YesOrNoEnum;
import com.ywt.user.domain.entity.User;
import com.ywt.websocket.domain.enums.WSRespTypeEnum;
import com.ywt.websocket.domain.vo.message.*;
import com.ywt.websocket.domain.vo.resp.WSBaseResp;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月18日 23:08
 */
public class WebSocketAdapter {

    /**
     * 封装登录的消息
     *
     * @param wxMpQrCodeTicket 二维码信息
     * @return
     */
    public static WSBaseResp<?> buildResp(WxMpQrCodeTicket wxMpQrCodeTicket) {
        String url = wxMpQrCodeTicket.getUrl();
        WSBaseResp<WSLoginUrl> wsLoginUrlWSBaseResp = new WSBaseResp<>();
        wsLoginUrlWSBaseResp.setType(WSRespTypeEnum.LOGIN_URL.getType());
        wsLoginUrlWSBaseResp.setData(WSLoginUrl.builder().loginUrl(url).build());
        return wsLoginUrlWSBaseResp;
    }

    /**
     * 封装，已经扫码，等待授权的消息
     *
     * @return
     */
    public static WSBaseResp<?> buildScanSuccessResp() {
        WSBaseResp<WSMessage> message = new WSBaseResp<>();
        message.setType(WSRespTypeEnum.LOGIN_SCAN_SUCCESS.getType());
        return message;
    }

    /**
     * 封装，已经授权成功，登录成功的消息
     */
    public static WSBaseResp<?> buildLoginSuccessResp(User user, String token, boolean hasPower) {
        WSBaseResp<WSLoginSuccess> wsLoginSuccessWSBaseResp = new WSBaseResp<>();
        wsLoginSuccessWSBaseResp.setData(WSLoginSuccess.builder()
                .uid(user.getId())
                .avatar(user.getAvatar())
                .name(user.getName())
                .power(hasPower ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode())
                .token(token)
                .build());
        wsLoginSuccessWSBaseResp.setType(WSRespTypeEnum.LOGIN_SUCCESS.getType());
        return wsLoginSuccessWSBaseResp;
    }

    /**
     * 登录token失效，给前端返回删除token信息
     */
    public static WSBaseResp<?> buildLoginLose() {
        WSBaseResp<WSMessage> message = new WSBaseResp<>();
        message.setType(WSRespTypeEnum.INVALIDATE_TOKEN.getType());
        return message;
    }

    public static WSBaseResp<WSBlack> buildBlackResp(User user) {
        WSBaseResp<WSBlack> wsBlackWSBaseResp = new WSBaseResp<>();
        wsBlackWSBaseResp.setType(WSRespTypeEnum.BLACK.getType());
        WSBlack wsBlack = new WSBlack();
        wsBlack.setUid(user.getId());
        wsBlackWSBaseResp.setData(wsBlack);
        return wsBlackWSBaseResp;
    }

    public static WSBaseResp<?> buildPushMsg(ChatMessageResp msgResp) {
        WSBaseResp<ChatMessageResp> chatMessageRespWSBaseResp = new WSBaseResp<>();
        chatMessageRespWSBaseResp.setType(WSRespTypeEnum.MESSAGE.getType());
        chatMessageRespWSBaseResp.setData(msgResp);
        return chatMessageRespWSBaseResp;
    }

    public static WSBaseResp<?> buildApplySend(WSFriendApply wsFriendApply) {
        WSBaseResp<WSFriendApply> wsFriendApplyWSBaseResp = new WSBaseResp<>();
        wsFriendApplyWSBaseResp.setType(WSRespTypeEnum.APPLY.getType());
        wsFriendApplyWSBaseResp.setData(wsFriendApply);
        return wsFriendApplyWSBaseResp;
    }
}




