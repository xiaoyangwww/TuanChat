package com.ywt.user.dao;

import com.ywt.user.domain.entity.UserEmoji;
import com.ywt.user.domain.vo.Resp.user.UserEmojiResp;
import com.ywt.user.mapper.UserEmojiMapper;
import com.ywt.user.service.UserEmojiService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户表情包 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-05-10
 */
@Service
public class UserEmojiDao extends ServiceImpl<UserEmojiMapper, UserEmoji> {

    public List<UserEmoji> listByUid(Long uid) {
        return lambdaQuery().eq(UserEmoji::getUid,uid).list();
    }

    public int countByUid(Long uid) {
        return lambdaQuery().eq(UserEmoji::getUid, uid).count();

    }

    public UserEmoji getByUidAndURL(Long uid, String expressionUrl) {
        return lambdaQuery()
                .eq(UserEmoji::getUid,uid)
                .eq(UserEmoji::getExpressionUrl,expressionUrl)
                .one();

    }
}
