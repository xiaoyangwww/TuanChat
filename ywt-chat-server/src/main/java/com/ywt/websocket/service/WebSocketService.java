package com.ywt.websocket.service;

import com.ywt.user.domain.entity.User;
import com.ywt.websocket.domain.vo.message.WSBlack;
import com.ywt.websocket.domain.vo.resp.WSBaseResp;
import io.netty.channel.Channel;

public interface WebSocketService {
    /**
     * 请求登录二维码,使用一个随机的code保存游客的channel
     * @param channel
     */
    void handleLoginReq(Channel channel);

    /**
     * 处理所有ws连接的事件
     * @param channel
     */
    void connect(Channel channel);

    /**
     * 用户下线
     * @param channel
     */
    void userOffLine(Channel channel);

    /**
     * 通过websocket 发送已经扫码，等待授权的消息
     */
    void handleScanSuccess(Integer code);

    /**
     * 通过websocket 发送已经授权,登录成功的消息
     */
    void handleAuthSuccess(Integer code, User user);

    /**
     * 登录认证，保持登录状态
     */
    void handleAuthorize(Channel channel, String token);


    /**
     * 发送用户被拉黑信息
     */
    void sendBlackMsg(WSBaseResp<WSBlack> wsBlackWSBaseResp);

    /**
     * 热点群聊消息，发送给全部在线用户
     * @param wsBaseMsg 发送的消息体
     * @param skipUid    需要跳过的人
     */
    void sendToAllOnline(WSBaseResp<?> wsBaseMsg, Long skipUid);

    /**
     * 普通群聊消息，发给相关在线用户
     */
    void sendToUid(WSBaseResp<?> wsBaseMsg, Long uid);
}
