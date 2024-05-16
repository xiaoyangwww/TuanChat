package com.ywt.websocket.service.adapter;

import cn.hutool.core.bean.BeanUtil;
import com.ywt.chat.domain.dto.ChatMessageMarkDTO;
import com.ywt.chat.domain.vo.Resp.ChatMemberStatisticResp;
import com.ywt.chat.domain.vo.Resp.ChatMessageResp;
import com.ywt.chat.service.ChatService;
import com.ywt.common.domain.enums.YesOrNoEnum;
import com.ywt.chat.domain.dto.ChatMsgRecallDTO;
import com.ywt.user.domain.entity.User;
import com.ywt.user.domain.enums.ChatActiveStatusEnum;
import com.ywt.websocket.domain.enums.WSRespTypeEnum;
import com.ywt.websocket.domain.vo.message.*;
import com.ywt.websocket.domain.vo.resp.WSBaseResp;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月18日 23:08
 */
@Component
public class WebSocketAdapter {

    @Autowired
    private ChatService chatService;

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

    public static WSBaseResp<?> buildRecallResp(ChatMsgRecallDTO chatMsgRecallDTO) {
        WSBaseResp<WSMsgRecall> wsMsgRecallWSBaseResp = new WSBaseResp<>();
        wsMsgRecallWSBaseResp.setType(WSRespTypeEnum.RECALL.getType());
        WSMsgRecall wsMsgRecall = new WSMsgRecall();
        BeanUtil.copyProperties(chatMsgRecallDTO,wsMsgRecall);
        wsMsgRecallWSBaseResp.setData(wsMsgRecall);
        return wsMsgRecallWSBaseResp;
    }

    public static WSBaseResp<?> buildMarkMsg(ChatMessageMarkDTO dto, Integer markCount) {
        WSMsgMark.WSMsgMarkItem item = new WSMsgMark.WSMsgMarkItem();
        BeanUtils.copyProperties(dto, item);
        item.setMarkCount(markCount);
        WSBaseResp<WSMsgMark> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WSRespTypeEnum.MARK.getType());
        WSMsgMark mark = new WSMsgMark();
        mark.setMarkList(Collections.singletonList(item));
        wsBaseResp.setData(mark);
        return wsBaseResp;

    }
    public WSBaseResp<WSOnlineOfflineNotify> buildOnlineNotifyResp(User user) {
        WSBaseResp<WSOnlineOfflineNotify> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WSRespTypeEnum.ONLINE_OFFLINE_NOTIFY.getType());
        WSOnlineOfflineNotify onlineOfflineNotify = new WSOnlineOfflineNotify();
        onlineOfflineNotify.setChangeList(Collections.singletonList(buildOnlineInfo(user)));
        assembleNum(onlineOfflineNotify);
        wsBaseResp.setData(onlineOfflineNotify);
        return wsBaseResp;
    }

    private ChatMemberResp buildOnlineInfo(User user) {
        return ChatMemberResp.builder()
                .activeStatus(ChatActiveStatusEnum.ONLINE.getType())
                .uid(user.getId())
                .lastOptTime(user.getLastOptTime())
                .build();
    }

    public WSBaseResp<?> buildOfflineNotifyResp(User user) {
        WSBaseResp<WSOnlineOfflineNotify> wsOnlineOfflineNotifyWSBaseResp = new WSBaseResp<>();
        wsOnlineOfflineNotifyWSBaseResp.setType(WSRespTypeEnum.ONLINE_OFFLINE_NOTIFY.getType());
        WSOnlineOfflineNotify wsOnlineOfflineNotify = WSOnlineOfflineNotify.builder()
                .changeList(Collections.singletonList(buildOfflineInfo(user)))
                .build();
        assembleNum(wsOnlineOfflineNotify);
        return wsOnlineOfflineNotifyWSBaseResp;
    }

    private void assembleNum(WSOnlineOfflineNotify onlineOfflineNotify) {
        ChatMemberStatisticResp memberStatistic = chatService.getMemberStatistic();
        onlineOfflineNotify.setOnlineNum(memberStatistic.getOnlineNum());
    }

    private static ChatMemberResp buildOfflineInfo(User user) {
        return ChatMemberResp.builder()
                .activeStatus(ChatActiveStatusEnum.OFFLINE.getType())
                .uid(user.getId())
                .lastOptTime(user.getLastOptTime())
                .build();
    }
}




