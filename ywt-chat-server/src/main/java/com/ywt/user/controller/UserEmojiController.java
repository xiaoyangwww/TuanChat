package com.ywt.user.controller;


import com.ywt.common.domain.vo.Req.IdReqVO;
import com.ywt.common.domain.vo.Resp.ApiResult;
import com.ywt.common.utils.RequestHolder;
import com.ywt.user.domain.vo.Req.user.UserEmojiReq;
import com.ywt.common.domain.vo.Resp.IdRespVO;
import com.ywt.user.domain.vo.Resp.user.UserEmojiResp;
import com.ywt.user.service.UserEmojiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 用户表情包 前端控制器
 * </p>
 *
 * @author ywt
 * @since 2024-05-10
 */
@RestController
@RequestMapping("/capi/user/emoji")
@Api(tags = "用户表情包管理相关接口")
public class UserEmojiController {

    @Autowired
    private UserEmojiService emojiService;

    @GetMapping("/list")
    @ApiOperation("表情包列表")
    public ApiResult<List<UserEmojiResp>> getEmojisPage() {
        return ApiResult.success(emojiService.getEmojisPage(RequestHolder.get().getUid()));
    }

    @PostMapping()
    @ApiOperation("新增表情包")
    public ApiResult<IdRespVO> insertEmojis(@Valid @RequestBody UserEmojiReq req) {
        return ApiResult.success(emojiService.insert(req, RequestHolder.get().getUid()));
    }

    @DeleteMapping()
    @ApiOperation("删除表情包")
    public ApiResult<Void> deleteEmojis(@Valid @RequestBody IdReqVO reqVO) {
        emojiService.remove(reqVO.getId(), RequestHolder.get().getUid());
        return ApiResult.success();
    }
}

