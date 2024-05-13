package com.ywt.chat.dao;

import cn.hutool.core.util.ObjectUtil;
import com.ywt.chat.domain.entity.Message;
import com.ywt.chat.domain.enums.MessageStatusEnum;
import com.ywt.chat.mapper.MessageMapper;
import com.ywt.chat.service.MessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywt.common.domain.vo.Req.ChatMessagePageReq;
import com.ywt.common.domain.vo.Req.CursorPageBaseReq;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;
import com.ywt.common.utils.CursorUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

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

    public CursorPageBaseResp<Message> getCursorPage(Long roomId, CursorPageBaseReq request, Long lastMsgId) {
        return CursorUtils.getCursorPageByMysql(this, request,
                wrapper -> wrapper
                        .eq(Message::getRoomId, roomId)
                        .eq(Message::getStatus, MessageStatusEnum.NORMAL.getStatus())
                        .le(ObjectUtil.isNotEmpty(lastMsgId),Message::getId, lastMsgId),
                Message::getId
        );
    }

    /**
     * 查询要回复的信息与当前信息的间隔数
     */
    public Integer getGapCount(Long roomId, Long replyMsgId, Long msgId) {
        return lambdaQuery()
                .le(Message::getId,msgId)
                .gt(Message::getId,replyMsgId)
                .eq(Message::getRoomId,roomId)
                .count();
    }

    /**
     * 获取未读数
     * @param roomId
     * @param readTime
     * @return
     */
    public Integer getUnReadCountMap(Long roomId, Date readTime) {
        return lambdaQuery().eq(Message::getRoomId, roomId)
                .gt(Objects.nonNull(readTime),Message::getCreateTime, readTime)
                .count();
    }
}
