package com.ywt.websocket.consumer;

import com.ywt.common.constant.MQConstant;
import com.ywt.common.domain.dto.PushMessageDTO;
import com.ywt.user.domain.enums.WSPushTypeEnum;
import com.ywt.websocket.service.WebSocketService;
import org.apache.rocketmq.spring.annotation.MessageModel;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月06日 11:33
 */
@RocketMQMessageListener(topic = MQConstant.PUSH_TOPIC,consumerGroup = MQConstant.PUSH_GROUP,messageModel = MessageModel.BROADCASTING)
@Component
public class PushConsumer implements RocketMQListener<PushMessageDTO> {

    @Autowired
    private WebSocketService webSocketService;

    @Override
    public void onMessage(PushMessageDTO pushMessageDTO) {
        WSPushTypeEnum wsPushTypeEnum = WSPushTypeEnum.of(pushMessageDTO.getPushType());
        switch (wsPushTypeEnum) {
            case ALL:
                webSocketService.sendToAllOnline(pushMessageDTO.getWsBaseMsg(), null);
                break;
            case USER:
                pushMessageDTO.getUidList().forEach(uid -> {
                    webSocketService.sendToUid(pushMessageDTO.getWsBaseMsg(), uid);
                });
                break;
        }
    }
}
