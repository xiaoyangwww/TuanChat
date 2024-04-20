package com.ywt.test;

import com.ywt.user.dao.UserDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月15日 17:49
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestUser {


    @Autowired
    private UserDao userDao;

    @Test
    public void test() {
        int count = userDao.count();
        System.out.println("count = " + count);
    }
}
