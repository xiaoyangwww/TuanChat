package com.ywt.user.domain.vo.Req.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Description: 表情包反参
 * Author: ywt
 * Date: 2023-07-09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEmojiReq {
    /**
     * 表情地址
     */
    @ApiModelProperty(value = "新增的表情url")
    private String expressionUrl;

}
