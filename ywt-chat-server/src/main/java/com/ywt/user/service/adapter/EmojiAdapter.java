package com.ywt.user.service.adapter;

import com.ywt.user.domain.entity.UserEmoji;
import com.ywt.user.domain.vo.Resp.user.UserEmojiResp;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年05月10日 15:48
 */
public class EmojiAdapter {

    public static List<UserEmojiResp> buildUserEmojiResp(List<UserEmoji> emojisPage) {
        return emojisPage.stream().map(item -> {
            UserEmojiResp userEmojiResp = new UserEmojiResp();
            userEmojiResp.setId(item.getId());
            userEmojiResp.setExpressionUrl(item.getExpressionUrl());
            return userEmojiResp;
        }).collect(Collectors.toList());
    }
}
