package com.ywt.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.interfaces.Claim;
import com.ywt.common.constant.RedisKey;
import com.ywt.common.utils.JwtUtils;
import com.ywt.user.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ywt.chat.common.utils.RedisUtils;

import java.util.concurrent.TimeUnit;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月20日 12:47
 */
@Service
public class LoginServiceImpl implements LoginService {

    /**
     * token 过期时间
     */
    public static final int TOKEN_EXPIRE_DAYS = 3;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    @Async
    public void renewalTokenIfNecessary(String token) {
        Long uid = getValidUid(token);
        if (uid == null) return;
        String key = getUidKey(uid);
        Long expire = RedisUtils.getExpire(key, TimeUnit.DAYS);
        // -2 说明找不到key
        if (expire == -2) return;
        //小于一天的token帮忙续期
        if (expire < 1) {
            RedisUtils.expire(key,TOKEN_EXPIRE_DAYS,TimeUnit.DAYS);
        }


    }

    @Override
    public String login(Long uid) {
        String key = RedisKey.getKey(RedisKey.USER_TOKEN_STRING, uid);
        String token = RedisUtils.getStr(key);
        if (StrUtil.isNotBlank(token)) {
            return token;
        }
        //获取用户token
        token = jwtUtils.createToken(uid);
        RedisUtils.set(key, token, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);//token过期用redis中心化控制，初期采用5天过期，剩1天自动续期的方案。后续可以用双token实现
        return token;
    }

    private static String getUidKey(Long uid) {
        return RedisKey.getKey(RedisKey.USER_TOKEN_STRING, uid);
    }

    @Override
    public Long getValidUid(String token) {
        Long uid = jwtUtils.getUidOrNull(token);
        if (uid == null) {
            return null;
        }
        String oldToken = RedisUtils.getStr(getUidKey(uid));
        if (token.equals(oldToken)) {
            return uid;
        }
        return null;
    }

}
