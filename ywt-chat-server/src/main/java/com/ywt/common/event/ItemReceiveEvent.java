package com.ywt.common.event;

import com.ywt.user.domain.entity.ItemConfig;
import com.ywt.user.domain.entity.UserBackpack;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;



/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年05月02日 9:55
 */
@Getter
public class ItemReceiveEvent extends ApplicationEvent {

    private final UserBackpack userBackpack;

    public ItemReceiveEvent(Object source, UserBackpack userBackpack) {
        super(source);
        this.userBackpack = userBackpack;
    }
}
