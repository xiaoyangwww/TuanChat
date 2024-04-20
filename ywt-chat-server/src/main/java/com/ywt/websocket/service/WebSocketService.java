package com.ywt.websocket.service;

import com.ywt.user.domain.entity.User;
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
}
