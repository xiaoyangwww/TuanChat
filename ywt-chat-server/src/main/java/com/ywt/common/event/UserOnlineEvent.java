package com.ywt.common.event;

import com.ywt.user.domain.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月25日 11:11
 */
@Getter
public class UserOnlineEvent extends ApplicationEvent {

    private final User user;

    public UserOnlineEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
