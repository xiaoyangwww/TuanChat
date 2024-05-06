package com.ywt.chat.service.adapter;

import cn.hutool.core.bean.BeanUtil;
import com.ywt.chat.domain.entity.Message;
import com.ywt.chat.domain.entity.MessageMark;
import com.ywt.chat.domain.enums.MessageMarkTypeEnum;
import com.ywt.chat.domain.enums.MessageStatusEnum;
import com.ywt.chat.domain.enums.MessageTypeEnum;
import com.ywt.chat.domain.vo.Req.ChatMessageReq;
import com.ywt.chat.domain.vo.Req.msg.TextMsgReq;
import com.ywt.chat.domain.vo.Resp.ChatMessageResp;
import com.ywt.chat.service.strategy.msg.AbstractMsgHandler;
import com.ywt.chat.service.strategy.msg.MsgHandlerFactory;
import com.ywt.common.domain.enums.YesOrNoEnum;
import org.checkerframework.checker.units.qual.C;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 功能描述
 * 消息适配器
 * @author: scott
 * @date: 2024年05月03日 11:59
 */
public class MessageAdapter {


    public static Message buildMsgSave(ChatMessageReq request, Long uid) {
        return Message.builder()
                .fromUid(uid)
                .roomId(request.getRoomId())
                .type(request.getMsgType())
                .status(MessageStatusEnum.NORMAL.getStatus())
                .build();
    }

    public static List<ChatMessageResp> buildMsgResp(List<Message> messages, List<MessageMark> msgMark, Long receiveUid) {
        Map<Long, List<MessageMark>> markMap = msgMark.stream().collect(Collectors.groupingBy(MessageMark::getMsgId));
        return messages.stream().map(a -> {
                    ChatMessageResp resp = new ChatMessageResp();
                    resp.setFromUser(buildFromUser(a.getFromUid()));
                    resp.setMessage(buildMessage(a, markMap.getOrDefault(a.getId(), new ArrayList<>()), receiveUid));
                    return resp;
                })
                .sorted(Comparator.comparing(a -> a.getMessage().getSendTime()))//帮前端排好序，更方便它展示
                .collect(Collectors.toList());
    }

    private static ChatMessageResp.Message buildMessage(Message message, List<MessageMark> marks, Long receiveUid) {
        ChatMessageResp.Message messageVO = new ChatMessageResp.Message();
        BeanUtil.copyProperties(message, messageVO);
        messageVO.setSendTime(message.getCreateTime());
        AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategyNoNull(message.getType());
        if (Objects.nonNull(msgHandler)) {
            messageVO.setBody(msgHandler.showMsg(message));
        }
        //消息标记
        messageVO.setMessageMark(buildMsgMark(marks, receiveUid));
        return messageVO;
    }

    private static ChatMessageResp.MessageMark buildMsgMark(List<MessageMark> marks, Long receiveUid) {
        Map<Integer, List<MessageMark>> typeMap = marks.stream().collect(Collectors.groupingBy(MessageMark::getType));
        List<MessageMark> likeMarks = typeMap.getOrDefault(MessageMarkTypeEnum.LIKE.getType(), new ArrayList<>());
        List<MessageMark> dislikeMarks = typeMap.getOrDefault(MessageMarkTypeEnum.DISLIKE.getType(), new ArrayList<>());
        ChatMessageResp.MessageMark mark = new ChatMessageResp.MessageMark();
        mark.setLikeCount(likeMarks.size());
        mark.setUserLike(Optional.ofNullable(receiveUid).filter(uid -> likeMarks.stream().anyMatch(a -> Objects.equals(a.getUid(), uid))).map(a -> YesOrNoEnum.YES.getCode()).orElse(YesOrNoEnum.NO.getCode()));
        mark.setDislikeCount(dislikeMarks.size());
        mark.setUserDislike(Optional.ofNullable(receiveUid).filter(uid -> dislikeMarks.stream().anyMatch(a -> Objects.equals(a.getUid(), uid))).map(a -> YesOrNoEnum.YES.getCode()).orElse(YesOrNoEnum.NO.getCode()));
        return mark;
    }

    private static ChatMessageResp.UserInfo buildFromUser(Long fromUid) {
        ChatMessageResp.UserInfo userInfo = new ChatMessageResp.UserInfo();
        userInfo.setUid(fromUid);
        return userInfo;
    }

    public static ChatMessageReq buildAgreeMsg(Long roomId) {
        return ChatMessageReq.builder()
                .roomId(roomId)
                .msgType(MessageTypeEnum.TEXT.getType())
                .body(TextMsgReq.builder().content("我们已经成为好友了，开始聊天吧").build())
                .build();


    }
}
