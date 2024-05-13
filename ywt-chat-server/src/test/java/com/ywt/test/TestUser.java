package com.ywt.test;

import com.ywt.oss.MinIOTemplate;
import com.ywt.oss.domain.OssReq;
import com.ywt.oss.domain.OssResp;
import com.ywt.user.dao.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import ywt.chat.common.utils.RedisUtils;

import java.util.concurrent.TimeUnit;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月15日 17:49
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class TestUser {


    @Autowired
    private UserDao userDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MinIOTemplate minIOTemplate;



    @Test
    public void test() {
        RedisUtils.set("ywt","123",3L, TimeUnit.MINUTES);
    }

    @Test
    public void getUploadUrl() {
        OssReq ossReq = OssReq.builder()
                .fileName("test.jpeg")
                .filePath("/test")
                .autoPath(false)
                .build();
        OssResp preSignedObjectUrl = minIOTemplate.getPreSignedObjectUrl(ossReq);
        System.out.println(preSignedObjectUrl);
    }


}
