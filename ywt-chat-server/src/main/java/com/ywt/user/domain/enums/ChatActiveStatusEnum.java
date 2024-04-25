package com.ywt.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChatActiveStatusEnum {

    ONLINE(1, "用户上线"),
    OFFLINE(2, "用户下线"),
    ;

    private final Integer type;
    private final String desc;
}