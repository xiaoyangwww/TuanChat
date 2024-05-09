package com.ywt.common.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月22日 10:22
 */
@AllArgsConstructor
@Getter
public enum YesOrNoEnum {
    NO(0, "否"),
    YES(1, "是"),
    ;


    private final Integer code;

    private final String mess;

    public static Integer toStatus(boolean b) {
        return b ? YES.code : NO.code;
    }
}
