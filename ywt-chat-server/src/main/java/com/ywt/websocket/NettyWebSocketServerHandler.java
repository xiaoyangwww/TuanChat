package com.ywt.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sun.org.apache.bcel.internal.generic.SWITCH;
import com.ywt.websocket.domain.enums.WSReqTypeEnum;
import com.ywt.websocket.domain.vo.req.WSBaseReq;
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

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            System.out.println("websocket 握手完成");
        } else if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("读空闲");
                // TODO 用户下线
                ctx.channel().close();
            }
        }
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
                System.out.println("登录二维码");
                ctx.writeAndFlush(new TextWebSocketFrame("123"));
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
