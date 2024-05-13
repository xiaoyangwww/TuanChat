package com.ywt.test;

import com.ywt.common.constant.RedisKey;
import com.ywt.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ywt.chat.common.utils.RedisUtils;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年05月02日 11:54
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class TokenTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    public void createToken() {
        String token = jwtUtils.createToken(11004L);
        RedisUtils.set(RedisKey.getKey(RedisKey.USER_TOKEN_STRING,11004),token);

    }

    @Test
    public void getUid() {
        Long uidOrNull = jwtUtils.getUidOrNull("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjExMDA0LCJjcmVhdGVUaW1lIjoxNzE0NjIyMzMzfQ.m0WlqJVmThemFyOIVt3PU-04OSAwiwUY0jmTsyYC3jI");
        System.out.println("uidOrNull = " + uidOrNull);
    }
}
