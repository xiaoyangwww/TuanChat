package com.ywt.user.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

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
@Api(value = "用户管理相关接口")
public class UserController {

    @GetMapping("/public/userInfo")
    @ApiOperation("获取用户信息")
    public String getUserById(@RequestParam Long uid) {
        return "123";
    }

}

