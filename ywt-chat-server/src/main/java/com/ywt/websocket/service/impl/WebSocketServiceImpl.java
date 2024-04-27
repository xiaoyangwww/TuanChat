package com.ywt.websocket.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ywt.common.event.UserOnlineEvent;
import com.ywt.common.utils.NettyUtil;
import com.ywt.config.ThreadPoolConfig;
import com.ywt.user.cache.UserCache;
import com.ywt.user.dao.UserDao;
import com.ywt.user.domain.entity.IpInfo;
import com.ywt.user.domain.entity.User;
import com.ywt.user.domain.enums.RoleEnum;
import com.ywt.user.service.LoginService;
import com.ywt.user.service.RoleService;
import com.ywt.websocket.domain.dto.WSChannelExtraDTO;
import com.ywt.websocket.domain.vo.message.WSBlack;
import com.ywt.websocket.domain.vo.resp.WSBaseResp;
import com.ywt.websocket.service.WebSocketService;
import com.ywt.websocket.service.adapter.WebSocketAdapter;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月18日 21:59
 */
@Service
public class WebSocketServiceImpl implements WebSocketService {

    /**
     * 设置Caffeine map 的最大容量
     */
    public static final int MAXIMUM_SIZE = 1000;
    /**
     * 设置数据的过期时间
     */
    public static final Duration EXPIRE_TIME = Duration.ofHours(1);
    /**
     * code -> channel,等待用户扫码
     */
    public static final Cache<Integer, Channel> WAIT_LOGIN_MAP = Caffeine.newBuilder()
            .expireAfterWrite(EXPIRE_TIME)
            .maximumSize(MAXIMUM_SIZE)
            .build();


    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private RoleService roleService;

    /**
     * 所有已连接的websocket连接列表和一些额外参数(已经登录的用户) channel -> user
     */
    private static final ConcurrentHashMap<Channel, WSChannelExtraDTO> ONLINE_WS_MAP = new ConcurrentHashMap<>();

    @Autowired
    @Qualifier(ThreadPoolConfig.WS_EXECUTOR)
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    @Override
    public void handleLoginReq(Channel channel) {
        // 随机生成一个code
        Integer code = generateLoginCode(channel);
        // 存储channel
        WAIT_LOGIN_MAP.put(code, channel);
        // //请求微信接口，获取登录码地址，生成带有code的二维码
        WxMpQrCodeTicket wxMpQrCodeTicket;
        try {
            wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(code, (int) EXPIRE_TIME.getSeconds() * 5);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
        // 封装登录的消息通过websocket通道返回给用户
        sendMsg(channel, WebSocketAdapter.buildResp(wxMpQrCodeTicket));
    }

    @Override
    public void connect(Channel channel) {
        ONLINE_WS_MAP.put(channel, new WSChannelExtraDTO());
    }

    @Override
    public void userOffLine(Channel channel) {
        ONLINE_WS_MAP.remove(channel);
    }

    @Override
    public void handleScanSuccess(Integer code) {
        Channel channel = WAIT_LOGIN_MAP.getIfPresent(code);
        if (channel == null) {
            return;
        }
        sendMsg(channel, WebSocketAdapter.buildScanSuccessResp());
    }

    @Override
    public void handleAuthSuccess(Integer code, User user) {
        Channel channel = WAIT_LOGIN_MAP.getIfPresent(code);
        if (channel == null) {
            return;
        }
        // 获取token
        String token = loginService.login(user.getId());
        // 删除code -》 channel
        WAIT_LOGIN_MAP.invalidate(code);
        // 认证
        loginSuccess(channel, user, token);
    }

    @Override
    public void handleAuthorize(Channel channel, String token) {
        Long validUid = loginService.getValidUid(token);
        if (ObjUtil.isNull(validUid)) {
            // 登录token失效，给前端返回删除token信息
            sendMsg(channel, WebSocketAdapter.buildLoginLose());
            return;
        }
        User user = userDao.getById(validUid);
        // token 有效，登录成功
        loginSuccess(channel, user, token);

    }

    @Override
    public void sendBlackMsg(WSBaseResp<WSBlack> wsBlackWSBaseResp) {
        ONLINE_WS_MAP.forEach(((channel, wsChannelExtraDTO)
                -> threadPoolTaskExecutor.execute(() -> sendMsg(channel, wsBlackWSBaseResp))
        ));
    }

    private void loginSuccess(Channel channel, User user, String token) {
        WSChannelExtraDTO wsChannelExtraDTO = new WSChannelExtraDTO();
        wsChannelExtraDTO.setUid(user.getId());
        // ，建立连接 channel -> uid
        ONLINE_WS_MAP.put(channel, wsChannelExtraDTO);
        // 获取ip
        String ip = NettyUtil.getAttr(channel, NettyUtil.IP);
        user.setLastOptTime(new Date());
        // 刷新用户的ip地址
        user.refreshIp(ip);
        // 登录成功发送消息，进行ip解析
        applicationEventPublisher.publishEvent(new UserOnlineEvent(this, user));
        boolean hasPower = roleService.hasPower(user.getId(), RoleEnum.CHAT_MANAGER);
        // 发送登录成功的信息
        sendMsg(channel, WebSocketAdapter.buildLoginSuccessResp(user, token, hasPower));
    }


    private static void sendMsg(Channel channel, WSBaseResp<?> message) {
        channel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(message)));
    }

    private Integer generateLoginCode(Channel channel) {
        // 生成随机的code
        int code;
        do {
            code = RandomUtil.randomInt(Integer.MAX_VALUE);
        } while (WAIT_LOGIN_MAP.asMap().containsKey(code));

        return code;
    }
}
