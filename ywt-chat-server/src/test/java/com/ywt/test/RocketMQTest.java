package com.ywt.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月03日 17:44
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class RocketMQTest {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Test
    public void sendMQ() {
        Message<String> build = MessageBuilder.withPayload("123").build();
        rocketMQTemplate.send("test-topic", build);
    }
}
