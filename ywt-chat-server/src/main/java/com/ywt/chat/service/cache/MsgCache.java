package com.ywt.chat.service.cache;

import com.ywt.chat.dao.MessageDao;
import com.ywt.chat.domain.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * 功能描述
 * 消息相关缓存
 * @author: scott
 * @date: 2024年05月08日 10:08
 */
@Component
public class MsgCache {

    @Autowired
    private MessageDao messageDao;

    @Cacheable(cacheNames = "msg",key = "'msg' + #msgId")
    public Message getMsg(Long msgId) {
        return messageDao.getById(msgId);
    }

    @CacheEvict(cacheNames = "msg",key = "'msg' + #msgId")
    public Message evictMsg(Long msgId) {
        return null;
    }
}
