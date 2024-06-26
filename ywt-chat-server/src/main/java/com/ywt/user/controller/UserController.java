package com.ywt.user.controller;


import com.ywt.common.domain.vo.Resp.ApiResult;
import com.ywt.common.utils.RequestHolder;
import com.ywt.user.domain.dto.ItemInfoDTO;
import com.ywt.user.domain.dto.SummeryInfoDTO;
import com.ywt.user.domain.vo.Req.user.*;
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

    @PostMapping("/public/summary/userInfo/batch")
    @ApiOperation("用户聚合信息-返回的代表需要刷新的")
    public ApiResult<List<SummeryInfoDTO>> getSummeryUserInfo(@Valid @RequestBody SummeryInfoReq req) {
        return ApiResult.success(userService.getSummeryUserInfo(req));
    }

    @PostMapping("/public/badges/batch")
    @ApiOperation("徽章聚合信息-返回的代表需要刷新的")
    public ApiResult<List<ItemInfoDTO>> getItemInfo(@Valid @RequestBody ItemInfoReq req) {
        return ApiResult.success(userService.getItemInfo(req));
    }



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

    @GetMapping("/badges")
    @ApiOperation("可选徽章列表")
    public ApiResult<List<BadgeResp>> badges() {
        return ApiResult.success(userService.badges(RequestHolder.get().getUid()));
    }

    @PutMapping("/badge")
    @ApiOperation("佩戴徽章")
    public ApiResult<Void> wearingBadge(@Valid @RequestBody WearingBadgeReq req) {
        userService.wearingBadge(RequestHolder.get().getUid(), req);
        return ApiResult.success();
    }

    @PutMapping("/black")
    @ApiOperation("拉黑用户")
    public ApiResult<Void> black(@Valid @RequestBody BlackReq req) {
        userService.black(req.getUid());
        return ApiResult.success();
    }


}

