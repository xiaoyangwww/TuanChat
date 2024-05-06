package com.ywt.chat.dao;

import com.ywt.chat.domain.entity.MessageMark;
import com.ywt.chat.mapper.MessageMarkMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywt.common.domain.enums.NormalOrNoEnum;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<MessageMark> getValidMarkByMsgIdBatch(List<Long> messageIds) {
        return lambdaQuery()
                .in(MessageMark::getMsgId, messageIds)
                .eq(MessageMark::getStatus, NormalOrNoEnum.NORMAL.getStatus())
                .list();

    }
}
