package com.ywt.user.service;

import com.ywt.user.domain.vo.Req.user.UserEmojiReq;
import com.ywt.common.domain.vo.Resp.IdRespVO;
import com.ywt.user.domain.vo.Resp.user.UserEmojiResp;

import java.util.List;

/**
 * <p>
 * 用户表情包 服务类
 * </p>
 *
 * @author ywt
 * @since 2024-05-10
 */
public interface UserEmojiService {

    /**
     * 获取表情包列表
     * @return
     */
    List<UserEmojiResp> getEmojisPage(Long uid);

    /**
     * 新增表情包
     */
    IdRespVO insert(UserEmojiReq req, Long uid);

    /**
     * 删除表情包
     */
    void remove(long id, Long uid);
}
