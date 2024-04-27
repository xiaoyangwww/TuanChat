package com.ywt.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description: 物品枚举
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-19
 */
@AllArgsConstructor
@Getter
public enum BlackTypeEnum {
    IP(1,"拉黑用户id"),
    UID(2,"拉黑用户ip"),
    ;

    private final Integer type;
    private final String desc;

}
