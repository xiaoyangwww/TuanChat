package com.ywt.chat.controller;


import com.ywt.chat.domain.vo.Req.ChatMessageReq;
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

}

