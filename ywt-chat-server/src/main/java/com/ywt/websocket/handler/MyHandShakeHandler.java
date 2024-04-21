package com.ywt.websocket.handler;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlPath;
import com.ywt.common.utils.NettyUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.apache.http.client.utils.URIBuilder;

import java.nio.charset.Charset;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 功能描述
 *  获取token的handler
 * @author: scott
 * @date: 2024年04月21日 15:24
 */
public class MyHandShakeHandler  extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final HttpObject httpObject = (HttpObject) msg;
        if (httpObject instanceof HttpRequest) {
            final HttpRequest req = (HttpRequest) httpObject;
            // 获取token参数
            UrlBuilder urlBuilder = UrlBuilder.ofHttp(req.uri());
            Optional<String> tokenOptional = Optional.of(urlBuilder)
                    .map(UrlBuilder::getQuery)
                    .map(k -> k.get("token"))
                    .map(CharSequence::toString);
            tokenOptional.ifPresent(s -> NettyUtil.setAttr(ctx.channel(),NettyUtil.TOKEN,s));
            // 要将uri的路径改为 / ,不然webSocket无法建立连接
            req.setUri(urlBuilder.getPath().toString());
        }
        // 执行下一个handler
        ctx.fireChannelRead(msg);
    }
}
