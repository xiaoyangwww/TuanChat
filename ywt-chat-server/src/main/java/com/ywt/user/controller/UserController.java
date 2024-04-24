package com.ywt.user.controller;


import com.ywt.common.domain.vo.Resp.ApiResult;
import com.ywt.common.utils.RequestHolder;
import com.ywt.user.domain.vo.Req.user.ModifyNameReq;
import com.ywt.user.domain.vo.Resp.user.BadgeResp;
import com.ywt.user.domain.vo.Resp.user.UserInfoResp;
import com.ywt.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author ywt
 * @since 2024-04-15
 */
@RestController
@RequestMapping("/capi/user")
@Api(tags = "用户管理相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/userInfo")
    @ApiOperation("获取用户信息")
    public ApiResult<UserInfoResp> getUserById() {
        UserInfoResp userInfoResp = userService.getUserInfo(RequestHolder.get().getUid());
        return ApiResult.success(userInfoResp);
    }

    @PostMapping("/name")
    @ApiOperation("修改用户名")
    public ApiResult<Void> modifyName(@Valid @RequestBody ModifyNameReq modifyNameReq) {
         userService.modifyName(modifyNameReq);
         return ApiResult.success();
    }

    @GetMapping("/badge")
    @ApiOperation("可选徽章列表")
    public ApiResult<List<BadgeResp>> badge() {
        return ApiResult.success(userService.badges(RequestHolder.get().getUid()));
    }

}

