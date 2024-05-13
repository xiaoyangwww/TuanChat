package com.ywt.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 功能描述
 * 幂等枚举
 * @author: ywt
 * @date: 2024年04月23日 15:55
 */
@AllArgsConstructor
@Getter
public enum IdempotentEnum {

    UID(1, "uid"),
    MSG_ID(2, "信息id"),
    ;

    private final Integer type;
    private final String desc;
}
