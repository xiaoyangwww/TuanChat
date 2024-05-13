package com.ywt.user.domain.vo.Req.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月22日 11:04
 */
@Data
public class ModifyNameReq {

    @NotNull
    @Length(max = 6,message = "用户名可别取太长，不然我记不住噢")
    @ApiModelProperty("用户名")
    private String name;
}
