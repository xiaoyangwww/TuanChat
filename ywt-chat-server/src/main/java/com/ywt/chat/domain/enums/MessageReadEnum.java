package com.ywt.chat.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 消息已读未读
 * Author: ywt
 * Date: 2023-03-19
 */
@AllArgsConstructor
@Getter
public enum MessageReadEnum {
    READ(1L, "已读"),
    UNREAD(2L, "未读"),
    ;

    private final Long type;
    private final String desc;




}
