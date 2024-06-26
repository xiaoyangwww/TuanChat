package com.ywt.user.domain.vo.Req.friend;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 功能描述
 * Description: 申请好友信息
 * @author: ywt
 * @date: 2024年05月01日 15:41
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendApplyReq {

    @NotBlank
    @ApiModelProperty("申请信息")
    private String msg;

    @NotBlank
    @ApiModelProperty("好友uid")
    private Long targetUid;

}
