package com.ywt.common.event.listener;

import com.ywt.chat.service.PushService;
import com.ywt.chat.service.cache.MsgCache;
import com.ywt.common.event.MessageRecallEvent;
import com.ywt.websocket.domain.vo.resp.WSBaseResp;
import com.ywt.websocket.service.adapter.WebSocketAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月08日 10:14
 */
@Component
public class MessageRecallListener {

    @Autowired
    private PushService pushService;

    @Autowired
    private MsgCache msgCache;

    @Async
    @EventListener(classes = MessageRecallEvent.class)
    public void evictMsg(MessageRecallEvent event){
        Long msgId = event.getChatMsgRecallDTO().getMsgId();
        msgCache.evictMsg(msgId);
    }


    @Async
    @EventListener(classes = MessageRecallEvent.class)
    public void sendAll(MessageRecallEvent event) {
        pushService.sendPushMsg(WebSocketAdapter.buildRecallResp(event.getChatMsgRecallDTO()));
    }

}
