package com.ywt.user.config;

import com.ywt.user.service.handler.LogHandler;
import com.ywt.user.service.handler.MsgHandler;
import com.ywt.user.service.handler.ScanHandler;
import com.ywt.user.service.handler.SubscribeHandler;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.List;
import java.util.stream.Collectors;

import static me.chanjar.weixin.common.api.WxConsts.EventType.SUBSCRIBE;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType.EVENT;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月17日 16:27
 */
@AllArgsConstructor
@Configuration
@EnableConfigurationProperties(WxMpProperties.class)
public class WxMpConfiguration {
    private final LogHandler logHandler;
    private final MsgHandler msgHandler;
    private final SubscribeHandler subscribeHandler;
    private final ScanHandler scanHandler;
    private final WxMpProperties properties;

    /**
     * 这段代码的作用是创建一个WxMpService实例，并配置多个微信公众号的参数信息。
     * @return
     */
    @Bean
    public WxMpService wxMpService() {
        // 代码里 getConfigs()处报错的同学，请注意仔细阅读项目说明，你的IDE需要引入lombok插件！！！！
        final List<WxMpProperties.MpConfig> configs = this.properties.getConfigs();
        if (configs == null) {
            throw new RuntimeException("大哥，拜托先看下项目首页的说明（readme文件），添加下相关配置，注意别配错了！");
        }

        WxMpService service = new WxMpServiceImpl();
        service.setMultiConfigStorages(configs
                .stream().map(a -> {
                    WxMpDefaultConfigImpl configStorage;
                    configStorage = new WxMpDefaultConfigImpl();
                    configStorage.setAppId(a.getAppId());
                    configStorage.setSecret(a.getSecret());
                    configStorage.setToken(a.getToken());
                    configStorage.setAesKey(a.getAesKey());
                    return configStorage;
                }).collect(Collectors.toMap(WxMpDefaultConfigImpl::getAppId, a -> a, (o, n) -> o)));
        return service;
    }

    /**
     * 作用是创建一个WxMpMessageRouter实例，并配置了消息路由的规则，用于根据不同的消息类型和事件类型来分发和处理微信公众号的消息。
     * @param wxMpService
     * @return
     */
    @Bean
    public WxMpMessageRouter messageRouter(WxMpService wxMpService) {
        // 创建了一个WxMpMessageRouter实例，并传入了一个WxMpService实例作为参数，用于处理微信公众号的消息。
        final WxMpMessageRouter newRouter = new WxMpMessageRouter(wxMpService);

        // 记录所有事件的日志 （异步执行）
        newRouter.rule().handler(this.logHandler).next();

        // 关注事件
        //这段代码配置了一个规则，用于处理关注事件（用户关注公众号）。.async(false)表示不异步处理消息，.msgType(EVENT)表示消息类型
        // 关注事件，.handler(this.subscribeHandler)表示使用subscribeHandler来处理关注事件，.end()表示当前规则结束。
        // 为事件类型，.event(SUBSCRIBE)表示事件类型为.
        newRouter.rule().async(false).msgType(EVENT).event(SUBSCRIBE).handler(this.subscribeHandler).end();

        // 扫码事件
        newRouter.rule().async(false).msgType(EVENT).event(WxConsts.EventType.SCAN).handler(this.scanHandler).end();

        // 默认
        newRouter.rule().async(false).handler(this.msgHandler).end();

        return newRouter;
    }

}