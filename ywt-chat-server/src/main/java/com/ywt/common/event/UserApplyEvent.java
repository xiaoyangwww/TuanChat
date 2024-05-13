package com.ywt.common.event;

import com.ywt.user.domain.entity.UserApply;
import com.ywt.user.service.impl.UserFriendServiceImpl;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年05月01日 16:23
 */
@Getter
public class UserApplyEvent extends ApplicationEvent {

    private final UserApply userApply;

    public UserApplyEvent(Object source,UserApply userApply) {
        super(source);
        this.userApply = userApply;
    }
}
