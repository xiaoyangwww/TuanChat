package com.ywt.websocket.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ywt.common.event.UserBlackEvent;
import com.ywt.common.event.UserOnlineEvent;
import com.ywt.common.utils.NettyUtil;
import com.ywt.common.config.ThreadPoolConfig;
import com.ywt.user.cache.UserCache;
import com.ywt.user.dao.UserDao;
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
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月18日 21:59
 */
@Service
@Slf4j
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

    @Autowired
    private UserCache userCache;

    /**
     * 所有已连接的websocket连接列表和一些额外参数(已经登录的用户) channel -> user
     */
    private static final ConcurrentHashMap<Channel, WSChannelExtraDTO> ONLINE_WS_MAP = new ConcurrentHashMap<>();

    /**
     * 所有在线的用户和对应的socket
     */
    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<Channel>> ONLINE_UID_MAP = new ConcurrentHashMap<>();

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
        WSChannelExtraDTO wsChannelExtraDTO = ONLINE_WS_MAP.get(channel);
        Optional<Long> uidOptional = Optional.ofNullable(wsChannelExtraDTO).map(WSChannelExtraDTO::getUid);
        // 判断用户是否全部下线
        boolean offLine = offLine(channel,uidOptional);
        //已登录用户断连,并且全下线成功
        if (uidOptional.isPresent() && offLine) {
            User user = new User();
            user.setId(uidOptional.get());
            user.setLastOptTime(new Date());
            applicationEventPublisher.publishEvent(new UserBlackEvent(this,user));
        }

    }

    private boolean offLine(Channel channel, Optional<Long> uidOptional) {
        ONLINE_WS_MAP.remove(channel);
        if (uidOptional.isPresent()) {
            CopyOnWriteArrayList<Channel> channels = ONLINE_UID_MAP.get(uidOptional.get());
            if (CollectionUtil.isNotEmpty(channels)) {
                channels.removeIf(ch -> Objects.equals(ch, channel));
            }
            return CollectionUtil.isEmpty(ONLINE_UID_MAP.get(uidOptional.get()));
        }
        return true;
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

    @Override
    public void sendToAllOnline(WSBaseResp<?> wsBaseMsg, Long skipUid) {
        ONLINE_WS_MAP.forEach(((channel, wsChannelExtraDTO) -> {
            if (ObjectUtil.isNotNull(skipUid) && wsChannelExtraDTO.getUid().equals(skipUid)) {
                return;
            }
            threadPoolTaskExecutor.execute(() -> sendMsg(channel,wsBaseMsg));
        }));
    }

    @Override
    public void sendToUid(WSBaseResp<?> wsBaseMsg, Long uid) {
        CopyOnWriteArrayList<Channel> channels = ONLINE_UID_MAP.get(uid);
        if (channels.isEmpty()) {
            log.info("用户：{}不在线", uid);
            return;
        }
        channels.forEach(channel -> threadPoolTaskExecutor.execute(() -> sendMsg(channel, wsBaseMsg)));
    }

    @Override
    public Boolean scanLoginSuccess(Integer loginCode, Long uid) {
        //确认连接在该机器
        Channel channel = WAIT_LOGIN_MAP.getIfPresent(loginCode);
        if (Objects.isNull(channel)) {
            return Boolean.FALSE;
        }
        User user = userDao.getById(uid);
        //移除code
        WAIT_LOGIN_MAP.invalidate(loginCode);
        //调用用户登录模块
        String token = loginService.login(uid);
        //用户登录
        loginSuccess(channel, user, token);
        return Boolean.TRUE;
    }

    private void loginSuccess(Channel channel, User user, String token) {
        //更新上线列表
        online(channel, user.getId());
        user.setLastOptTime(new Date());
        boolean hasPower = roleService.hasPower(user.getId(), RoleEnum.CHAT_MANAGER);
        // 发送登录成功的信息
        sendMsg(channel, WebSocketAdapter.buildLoginSuccessResp(user, token, hasPower));
        //发送用户上线事件
        boolean online = userCache.isOnline(user.getId());
        if (!online) {
            // 刷新用户的ip地址
            user.refreshIp(NettyUtil.getAttr(channel, NettyUtil.IP));
            // 登录成功发送消息，进行ip解析
            applicationEventPublisher.publishEvent(new UserOnlineEvent(this, user));
        }
    }

    private void online(Channel channel, Long uid) {
        // ，建立连接 channel -> uid
        getOrInitChannelExt(channel,uid).setUid(uid);
        ONLINE_UID_MAP.putIfAbsent(uid,new CopyOnWriteArrayList<>());
        ONLINE_UID_MAP.get(uid).add(channel);
        NettyUtil.setAttr(channel, NettyUtil.UID, uid);

    }

    /**
     * 如果在线列表不存在，就先把该channel放进在线列表
     *
     * @param channel
     * @return
     */
    private WSChannelExtraDTO getOrInitChannelExt(Channel channel, Long uid) {
        WSChannelExtraDTO wsChannelExtraDTO = ONLINE_WS_MAP.getOrDefault(channel, new WSChannelExtraDTO());
        //如果该键已经存在，则不会进行任何操作，并返回之前与该键相关联的值。
        WSChannelExtraDTO old = ONLINE_WS_MAP.putIfAbsent(channel, wsChannelExtraDTO);
        return ObjectUtil.isNull(old) ? wsChannelExtraDTO : old;
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
