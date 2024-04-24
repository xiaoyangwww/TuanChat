package com.ywt.common.event;

import com.ywt.user.domain.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 功能描述
 *  用户注册事件
 * @author: scott
 * @date: 2024年04月24日 15:08
 */
@Getter
public class UserRegisterEvent extends ApplicationEvent {

    private final User user;

    public UserRegisterEvent(Object source,User user) {
        super(source);
        this.user = user;
    }
}
