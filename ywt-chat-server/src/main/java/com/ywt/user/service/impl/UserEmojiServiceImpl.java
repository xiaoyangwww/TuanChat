package com.ywt.user.service.impl;

import com.ywt.common.utils.AssertUtil;
import com.ywt.user.dao.UserEmojiDao;
import com.ywt.user.domain.entity.UserEmoji;
import com.ywt.user.domain.vo.Req.user.UserEmojiReq;
import com.ywt.common.domain.vo.Resp.IdRespVO;
import com.ywt.user.domain.vo.Resp.user.UserEmojiResp;
import com.ywt.user.service.UserEmojiService;
import com.ywt.user.service.adapter.EmojiAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年05月10日 15:35
 */
@Service
public class UserEmojiServiceImpl implements UserEmojiService {

    @Autowired
    private UserEmojiDao emojiDao;

    @Override
    public List<UserEmojiResp> getEmojisPage(Long uid) {
        List<UserEmoji> emojisPage = emojiDao.listByUid(uid);
        return EmojiAdapter.buildUserEmojiResp(emojisPage);
    }

    @Override
    public IdRespVO insert(UserEmojiReq req, Long uid) {
        //校验表情数量是否超过30
        int count = emojiDao.countByUid(uid);
        AssertUtil.isFalse(count >= 30,"最多只能添加30个表情哦~~");
        //校验表情是否存在
        UserEmoji userEmoji = emojiDao.getByUidAndURL(uid,req.getExpressionUrl());
        AssertUtil.isEmpty(userEmoji,"当前表情已存在哦~~");
        UserEmoji insert = new UserEmoji();
        insert.setUid(uid);
        insert.setExpressionUrl(req.getExpressionUrl());
        emojiDao.save(insert);
        return IdRespVO.id(insert.getId());
    }

    @Override
    public void remove(long id, Long uid) {
        UserEmoji userEmoji = emojiDao.getById(id);
        AssertUtil.isNotEmpty(userEmoji, "表情不能为空");
        AssertUtil.equal(userEmoji.getUid(), uid, "小黑子，别人表情不是你能删的");
        emojiDao.removeById(id);
    }


}
