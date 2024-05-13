package com.ywt.common.event.listener;


import com.sun.xml.internal.bind.v2.TODO;
import com.ywt.chat.domain.entity.Message;
import com.ywt.chat.domain.entity.Room;
import com.ywt.chat.transaction.service.MQProducer;
import com.ywt.common.constant.MQConstant;
import com.ywt.common.domain.dto.MsgSendMessageDTO;
import com.ywt.common.event.MessageSendEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年05月03日 15:36
 */
@Component
public class MessageSendListener {

    @Autowired
    private MQProducer mqProducer;


    @TransactionalEventListener(classes = MessageSendEvent.class,phase = TransactionPhase.BEFORE_COMMIT,fallbackExecution = true)
    public void messageRoute(MessageSendEvent event) {
        Long msgId = event.getMsgId();
        mqProducer.sendSecureMsg(MQConstant.SEND_MSG_TOPIC,new MsgSendMessageDTO(msgId),msgId);
    }

    @TransactionalEventListener(classes = MessageSendEvent.class, fallbackExecution = true)
    public void handlerMsg(MessageSendEvent event) {
        // TODO AI
//        Message message = messageDao.getById(event.getMsgId());
//        Room room = roomCache.get(message.getRoomId());
//        if (room.isHotRoom()) {
//            openAIService.chat(message);
//        }
    }



}
