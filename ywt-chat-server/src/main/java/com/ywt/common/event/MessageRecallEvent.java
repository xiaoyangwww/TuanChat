package com.ywt.common.event;

import com.ywt.user.domain.dto.ChatMsgRecallDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月08日 10:04
 */
@Getter
public class MessageRecallEvent extends ApplicationEvent {

    private final ChatMsgRecallDTO chatMsgRecallDTO;

    public MessageRecallEvent(Object source,ChatMsgRecallDTO chatMsgRecallDTO) {
        super(source);
        this.chatMsgRecallDTO = chatMsgRecallDTO;
    }
}
