package com.ywt.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sun.org.apache.bcel.internal.generic.SWITCH;
import com.ywt.common.utils.NettyUtil;
import com.ywt.websocket.domain.enums.WSReqTypeEnum;
import com.ywt.websocket.domain.vo.req.WSBaseReq;
import com.ywt.websocket.service.WebSocketService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月15日 15:15
 */
@ChannelHandler.Sharable
@Slf4j
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private WebSocketService webSocketService;

    /**
     * 方法会在 ChannelHandler 被添加到 ChannelPipeline 中时调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
         this.webSocketService = getService();

    }

    private WebSocketService getService() {
        return SpringUtil.getBean(WebSocketService.class);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            System.out.println("websocket 握手完成");
            // websocket 握手完成 就建立channel -> userid，游客也能收到消息
            webSocketService.connect(ctx.channel());
            // 获取token
            String token = NettyUtil.getAttr(ctx.channel(), NettyUtil.TOKEN);
            // 登录认证
            if (StrUtil.isNotEmpty(token)) {
                this.webSocketService.handleAuthorize(ctx.channel(),token);
            }
        } else if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("读空闲");
                // TODO 用户下线
//                userOffLine(ctx.channel());
            }
        }
    }

    /**
     * 连接断开时
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        userOffLine(ctx.channel());

    }

    private void userOffLine(Channel channel) {
        webSocketService.userOffLine(channel);
        channel.close();
        log.info("用户 {} 下线",channel);
    }


    /**
     * 读取客户端发送的请求报文
     * @param ctx
     * @param textWebSocketFrame
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        String json = textWebSocketFrame.text();
        WSBaseReq wsBaseReq = JSONUtil.toBean(json, WSBaseReq.class);
        WSReqTypeEnum wsReqTypeEnum = WSReqTypeEnum.of(wsBaseReq.getType());
        switch (wsReqTypeEnum) {
            case LOGIN:
                // 请求登录二维码,使用一个随机的code保存游客的channel
                this.webSocketService.handleLoginReq(ctx.channel());
                break;
            case AUTHORIZE:
                break;
            case HEARTBEAT:
                break;
            default:
                log.info("没有这种类型的请求 : {}",wsBaseReq.getType() );
        }

    }
}
