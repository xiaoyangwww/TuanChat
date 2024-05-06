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
}
