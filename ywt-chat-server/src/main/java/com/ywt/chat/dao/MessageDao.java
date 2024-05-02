package com.ywt.chat.dao;

import com.ywt.chat.domain.entity.Message;
import com.ywt.chat.mapper.MessageMapper;
import com.ywt.chat.service.MessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 消息表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
@Service
public class MessageDao extends ServiceImpl<MessageMapper, Message> {

}
