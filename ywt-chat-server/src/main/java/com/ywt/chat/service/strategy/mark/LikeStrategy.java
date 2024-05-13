package com.ywt.chat.service.strategy.mark;

import com.ywt.chat.domain.enums.MessageMarkTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月12日 10:01
 */
@Component
public class LikeStrategy extends AbstractMsgMarkStrategy{
    @Override
    protected MessageMarkTypeEnum getTypeEnum() {
        return MessageMarkTypeEnum.LIKE;
    }

    @Override
    public void doMark(Long uid, Long msgId) {
        super.doMark(uid, msgId);
        //同时取消点踩的动作
        MsgMarkFactory.getStrategyNoNull(MessageMarkTypeEnum.DISLIKE.getType()).unMark(uid,msgId);
    }
}
