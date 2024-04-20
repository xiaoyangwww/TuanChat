package com.ywt.user.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ywt.user.dao.UserDao;
import com.ywt.user.domain.entity.User;
import com.ywt.user.service.LoginService;
import com.ywt.user.service.UserService;
import com.ywt.user.service.WXService;
import com.ywt.user.service.adapter.TextBuilder;
import com.ywt.user.service.adapter.UserBuilder;
import com.ywt.websocket.service.WebSocketService;
import io.netty.channel.Channel;
import io.swagger.models.auth.In;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.time.Duration;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月20日 10:41
 */
@Service
public class WXServiceImpl implements WXService {

    @Autowired
    private UserDao userDao;

    @Value("${wx.mp.callback}")
    private String callback;

    @Autowired
    private UserService userService;

    private static final String URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";

    /**
     * 设置Caffeine map 的最大容量
     */
    public static final int MAXIMUM_SIZE = 1000;
    /**
     * 设置数据的过期时间
     */
    public static final Duration EXPIRE_TIME = Duration.ofHours(1);
    /**
     * 等待授权，openId -> code
     */
    public static final Cache<String, Integer> WAIT_AUTH_MAP = Caffeine.newBuilder()
            .expireAfterWrite(EXPIRE_TIME)
            .maximumSize(MAXIMUM_SIZE)
            .build();

    @Autowired
    @Lazy
    private WebSocketService webSocketService;




    @Override
    public WxMpXmlOutMessage scan(WxMpXmlMessage wxMpXmlMessage, WxMpService wxMpService) {

        String openId = wxMpXmlMessage.getFromUser();
        // //扫码关注的渠道事件有前缀，需要去除,qrscene_123
        String code = getEventKey(wxMpXmlMessage);
        // 判断用户是否存在
        User user = userDao.getUserByOpenId(openId);
        // 用户存在并且有权限，直接登录
        if(ObjectUtil.isNotNull(user) && StrUtil.isNotEmpty(user.getAvatar())) {
            return null;
        }
        // 用户不存在，注册
        if(ObjectUtil.isNull(user)) {
            User insert = UserBuilder.build(openId);
            // 在注册用户之前可能还会执行一些业务
            userService.register(insert);
        }
        // 保存openId 与 code 的对应关系
        WAIT_AUTH_MAP.put(openId,Integer.parseInt(code));
        // 通过websocket 发送已经扫码，等待授权的消息
        webSocketService.handleScanSuccess(Integer.parseInt(code));


        // 给用户发送用户授权信息
        String authUrl = String.format(URL, wxMpService.getWxMpConfigStorage().getAppId(), URLEncoder.encode(callback + "/wx/portal/public/callBack"));
        return TextBuilder.build("请点击链接授权：<a href=\"" + authUrl + "\">登录</a>", wxMpXmlMessage, wxMpService);
    }

    @Override
    public void authorize(WxOAuth2UserInfo userInfo) {
        // 将微信的用户信息保存
        User user = userDao.getUserByOpenId(userInfo.getOpenid());
        User update = UserBuilder.build(user.getId(), userInfo);
        userDao.updateById(update);

        Integer code = WAIT_AUTH_MAP.getIfPresent(userInfo.getOpenid());
        // 通过websocket 发送已经授权，登录成功的消息
        webSocketService.handleAuthSuccess(code,update);


    }

    private static String getEventKey(WxMpXmlMessage wxMpXmlMessage) {
        return wxMpXmlMessage.getEventKey().replace("qrscene_","");
    }
}
