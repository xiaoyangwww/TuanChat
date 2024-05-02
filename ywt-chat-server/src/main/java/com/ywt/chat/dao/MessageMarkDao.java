package com.ywt.chat.dao;

import com.ywt.chat.domain.entity.MessageMark;
import com.ywt.chat.mapper.MessageMarkMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 消息标记表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
@Service
public class MessageMarkDao extends ServiceImpl<MessageMarkMapper, MessageMark>  {

}
