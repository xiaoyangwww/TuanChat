package com.ywt.user.controller;


import com.ywt.common.domain.vo.Req.CursorPageBaseReq;
import com.ywt.common.domain.vo.Req.PageBaseReq;
import com.ywt.common.domain.vo.Resp.ApiResult;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;
import com.ywt.common.domain.vo.Resp.PageBaseResp;
import com.ywt.common.utils.RequestHolder;
import com.ywt.user.domain.vo.Req.friend.FriendApplyReq;
import com.ywt.user.domain.vo.Req.friend.FriendApproveReq;
import com.ywt.user.domain.vo.Req.friend.FriendCheckReq;
import com.ywt.user.domain.vo.Req.friend.FriendDeleteReq;
import com.ywt.user.domain.vo.Resp.friend.FriendApplyResp;
import com.ywt.user.domain.vo.Resp.friend.FriendCheckResp;
import com.ywt.user.domain.vo.Resp.friend.FriendResp;
import com.ywt.user.domain.vo.Resp.friend.FriendUnreadResp;
import com.ywt.user.service.UserFriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.validation.Valid;

/**
 * <p>
 * 用户联系人表 前端控制器
 * </p>
 *
 * @author ywt
 * @since 2024-04-29
 */
@RestController
@RequestMapping("/capi/user/friend")
@Api(tags = "好友相关接口")
public class FriendController {

    @Autowired
    private UserFriendService friendService;

    @GetMapping("/check")
    @ApiOperation("批量判断是否是自己好友")
    public ApiResult<FriendCheckResp> check(@Valid FriendCheckReq checkReq) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.check(uid, checkReq));
    }

    @PostMapping("/apply")
    @ApiOperation("申请好友")
    public ApiResult<Void> apply(@Valid @RequestBody FriendApplyReq request) {
        Long uid = RequestHolder.get().getUid();
        friendService.apply(uid,request);
        return ApiResult.success();
    }

    @PutMapping("/apply")
    @ApiOperation("审批同意")
    public ApiResult<Void> applyApprove(@Valid @RequestBody FriendApproveReq request) {
        friendService.applyApprove(RequestHolder.get().getUid(), request);
        return ApiResult.success();
    }

    @DeleteMapping()
    @ApiOperation("删除好友")
    public ApiResult<Void> delete(@Valid @RequestBody FriendDeleteReq request) {
        friendService.deleteFriend(RequestHolder.get().getUid(), request.getTargetUid());
        return ApiResult.success();
    }

    @GetMapping("/apply/unread")
    @ApiOperation("申请未读数")
    public ApiResult<FriendUnreadResp> unread() {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.unread(uid));
    }

    @GetMapping("/apply/page")
    @ApiOperation("好友申请列表")
    public ApiResult<PageBaseResp<FriendApplyResp>> page(@Valid PageBaseReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.getPageApplyFriend(uid, request));
    }


    /**
     * 联系人列表
     */
    @GetMapping("/page")
    @ApiOperation("联系人列表")
    public ApiResult<CursorPageBaseResp<FriendResp>> friendList(@Valid CursorPageBaseReq cursorPageBaseReq) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.friendList(uid, cursorPageBaseReq));
    }

}

