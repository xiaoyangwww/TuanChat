package com.ywt.chat.service.strategy.mark;

import cn.hutool.core.util.ObjectUtil;
import com.ywt.chat.dao.MessageMarkDao;
import com.ywt.chat.domain.dto.ChatMessageMarkDTO;
import com.ywt.chat.domain.entity.MessageMark;
import com.ywt.chat.domain.enums.MessageMarkActTypeEnum;
import com.ywt.chat.domain.enums.MessageMarkTypeEnum;
import com.ywt.common.domain.enums.YesOrNoEnum;
import com.ywt.common.event.MessageMarkEvent;
import com.ywt.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * 功能描述
 * 消息标记抽象类
 * @author: ywt
 * @date: 2024年05月12日 9:38
 */
public abstract class AbstractMsgMarkStrategy {

    @Autowired
    private MessageMarkDao messageMarkDao;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @PostConstruct
    public void init() {
        MsgMarkFactory.register(getTypeEnum().getType(),this);
    }

    protected abstract MessageMarkTypeEnum getTypeEnum();

    /**
     * 确认标记
     */
    @Transactional
    public void mark(Long uid,Long msgId) {
       doMark(uid,msgId);
    }

    /**
     * 取消标记
     */
    @Transactional
    public void unMark(Long uid,Long msgId) {
        doUnMark(uid,msgId);
    }

    public void doUnMark(Long uid, Long msgId) {
        exec(uid,msgId, MessageMarkActTypeEnum.UN_MARK);
    }

    public void doMark(Long uid, Long msgId) {
        exec(uid,msgId, MessageMarkActTypeEnum.MARK);
    }

    public void exec(Long uid, Long msgId,MessageMarkActTypeEnum actTypeEnum) {
        Integer actType = actTypeEnum.getType();
        Integer markType = getTypeEnum().getType();
        MessageMark oldMark = messageMarkDao.getByUidAndMsgId(uid,msgId,markType);
        if (ObjectUtil.isNull(oldMark) && actTypeEnum.equals(MessageMarkActTypeEnum.UN_MARK)) {
            //取消的类型，数据库一定有记录，没有就直接跳过操作
            return;
        }
        //插入一条新消息,或者修改一条消息
        MessageMark save = MessageMark.builder()
                .id(Optional.ofNullable(oldMark).map(MessageMark::getId).orElse(null))
                .uid(uid)
                .msgId(msgId)
                .type(getTypeEnum().getType())
                .status(transformAct(actType))
                .build();
        boolean modify = messageMarkDao.saveOrUpdate(save);
        if (modify) {
            //修改成功才发布消息标记事件
            ChatMessageMarkDTO dto = new ChatMessageMarkDTO(uid, msgId, markType, actType);
            applicationEventPublisher.publishEvent(new MessageMarkEvent(this, dto));
        }

    }

    private Integer transformAct(Integer actType) {
        if (actType == 1) {
            return YesOrNoEnum.NO.getCode();
        } else if (actType == 2) {
            return YesOrNoEnum.YES.getCode();
        }
        throw new BusinessException("动作类型 1确认 2取消");
    }
}
