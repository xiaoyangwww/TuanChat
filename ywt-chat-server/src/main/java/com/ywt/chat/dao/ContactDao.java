package com.ywt.chat.dao;

import com.ywt.chat.domain.entity.Contact;
import com.ywt.chat.domain.entity.Message;
import com.ywt.chat.domain.vo.Req.ChatMessageReadReq;
import com.ywt.chat.mapper.ContactMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywt.common.domain.vo.Req.CursorPageBaseReq;
import com.ywt.common.domain.vo.Resp.CursorPageBaseResp;
import com.ywt.common.utils.CursorUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 会话列表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
@Service
public class ContactDao extends ServiceImpl<ContactMapper, Contact> {

    public Contact get(Long roomId, Long uid) {
        return lambdaQuery().eq(Contact::getRoomId, roomId)
                .eq(Contact::getUid, uid)
                .one();

    }

    /**
     * 更新所有群成员的会话时间以及会话最新消息id
     */
    public void refreshOrCreateActiveTime(Long roomId, List<Long> memberUidList, Long msgId, Date createTime) {
        baseMapper.refreshOrCreateActiveTime(roomId, memberUidList, msgId, createTime);
    }

    /**
     * 用户的基础会话
     *
     * @param uid
     * @param request
     * @return
     */
    public CursorPageBaseResp<Contact> getContactPage(Long uid, CursorPageBaseReq request) {
        return CursorUtils.getCursorPageByMysql(this, request,
                wrapper -> wrapper
                        .eq(Contact::getUid, uid),
                Contact::getActiveTime
        );
    }

    public List<Contact> getByRoomIds(Long uid, List<Long> roomList) {
        return lambdaQuery().eq(Contact::getUid,uid)
                .in(Contact::getRoomId,roomList)
                .list();
    }

    public CursorPageBaseResp<Contact> getReadPage(ChatMessageReadReq request, Message message) {
        return CursorUtils.getCursorPageByMysql(this,request,
                wrapper -> wrapper
                        .ne(Contact::getUid,message.getFromUid())// 不需要查询出自己
                        .eq(Contact::getRoomId,message.getRoomId())
                        .ge(Contact::getReadTime,message.getCreateTime()),// 已读时间大于等于消息发送时间
                Contact::getReadTime
        );

    }

    public CursorPageBaseResp<Contact> getUnReadPage(ChatMessageReadReq cursorPageBaseReq, Message message) {
        return CursorUtils.getCursorPageByMysql(this, cursorPageBaseReq, wrapper -> {
            wrapper.eq(Contact::getRoomId, message.getRoomId());
            wrapper.ne(Contact::getUid, message.getFromUid());// 不需要查询出自己
            wrapper.lt(Contact::getReadTime, message.getCreateTime());// 已读时间小于消息发送时间
        }, Contact::getReadTime);
    }

    public Integer getTotalCount(Long roomId) {
        return lambdaQuery().eq(Contact::getRoomId, roomId).count();
    }

    public Integer getReadCount(Long roomId, Message message) {
        return lambdaQuery()
                .eq(Contact::getRoomId,roomId)
                .ne(Contact::getUid,message.getFromUid())
                .ge(Contact::getReadTime,message.getCreateTime())
                .count();
    }
}
