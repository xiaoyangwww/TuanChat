package com.ywt.chat.controller;


import com.ywt.chat.domain.dto.MsgReadInfoDTO;
import com.ywt.chat.domain.vo.Req.*;
import com.ywt.chat.domain.vo.Req.msg.ChatMessageMarkReq;
import com.ywt.chat.domain.vo.Resp.ChatMessageReadResp;
import com.ywt.chat.domain.vo.Resp.ChatMessageResp;
import com.ywt.chat.service.ChatService;
import com.ywt.common.domain.vo.Req.ChatMessagePageReq;
import com.ywt.common.domain.vo.Resp.ApiResult;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;
import com.ywt.common.utils.RequestHolder;
import com.ywt.user.cache.UserCache;
import com.ywt.user.domain.enums.BlackTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 群聊相关接口
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
@RestController
@RequestMapping("/capi/chat")
@Api(tags = "聊天室相关接口")
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserCache userCache;


    @PutMapping("/msg/mark")
    @ApiOperation("消息标记")
//    @FrequencyControl(time = 10, count = 5, target = FrequencyControl.Target.UID)
    public ApiResult<Void> setMsgMark(@Valid @RequestBody ChatMessageMarkReq request) {
        chatService.setMsgMark(RequestHolder.get().getUid(), request);
        return ApiResult.success();
    }


    @PutMapping("/msg/recall")
    @ApiOperation("撤回消息")
    public ApiResult<Void> recall(@Valid @RequestBody ChatMessageBaseReq request) {
         chatService.recallMsg(RequestHolder.get().getUid(),request);
         return ApiResult.success();
    }


    @GetMapping("/public/msg/page")
    @ApiOperation("消息列表")
    public ApiResult<CursorPageBaseResp<ChatMessageResp>> getMsgPage(@Valid ChatMessagePageReq request) {
        CursorPageBaseResp<ChatMessageResp> msgPage = chatService.getMsgPage(request, RequestHolder.get().getUid());
        // 过滤被拉黑的用户消息
        filterBlackMsg(msgPage);
        return ApiResult.success(msgPage);
    }

    private Set<String> getBlackUidSet() {
        return userCache.getBlackMap().getOrDefault(BlackTypeEnum.UID.getType(), new HashSet<>());
    }

    private void filterBlackMsg(CursorPageBaseResp<ChatMessageResp> msgPage) {
        Set<String> blackUidSet = getBlackUidSet();
        msgPage.getList().removeIf(item -> blackUidSet.contains(item.getFromUser().getUid().toString()));
    }


    @PostMapping("/msg")
    @ApiOperation("发送消息")
    public ApiResult<ChatMessageResp> sendMsg(@Valid @RequestBody ChatMessageReq request) {
        Long msgId = chatService.sendMsg(request, RequestHolder.get().getUid());
        //返回完整消息格式，方便前端展示
        return ApiResult.success(chatService.getMsgResp(msgId, RequestHolder.get().getUid()));
    }

    @GetMapping("/msg/read/page")
    @ApiOperation("消息的已读未读列表")
    public ApiResult<CursorPageBaseResp<ChatMessageReadResp>> getReadPage(@Valid ChatMessageReadReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(chatService.getReadPage(uid, request));
    }

    @GetMapping("/msg/read")
    @ApiOperation("获取消息的已读未读总数")
    public ApiResult<Collection<MsgReadInfoDTO>> getReadInfo(@Valid ChatMessageReadInfoReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(chatService.getMsgReadInfo(uid, request));
    }

    @PutMapping("/msg/read")
    @ApiOperation("消息阅读上报")
    public ApiResult<Void> msgRead(@Valid @RequestBody ChatMessageMemberReq request) {
        Long uid = RequestHolder.get().getUid();
        chatService.msgRead(uid, request);
        return ApiResult.success();
    }

}

