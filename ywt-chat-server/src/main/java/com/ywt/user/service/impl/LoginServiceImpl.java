package com.ywt.user.service.impl;

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
 * @author: scott
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
        String token = jwtUtils.createToken(uid);
        RedisUtils.set(getUidKey(uid), token, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
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
