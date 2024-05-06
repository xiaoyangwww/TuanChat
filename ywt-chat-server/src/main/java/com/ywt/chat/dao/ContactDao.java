package com.ywt.chat.dao;

import com.ywt.chat.domain.entity.Contact;
import com.ywt.chat.mapper.ContactMapper;
import com.ywt.chat.service.ContactService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class ContactDao extends ServiceImpl<ContactMapper, Contact>  {

    public Contact get(Long roomId, Long uid) {
        return lambdaQuery().eq(Contact::getRoomId, roomId)
                .eq(Contact::getUid, uid)
                .one();

    }

    /**
     * 更新所有群成员的会话时间以及会话最新消息id
     */
    public void refreshOrCreateActiveTime(Long roomId, List<Long> memberUidList, Long msgId, Date createTime) {
        baseMapper.refreshOrCreateActiveTime(roomId,memberUidList,msgId,createTime);
    }
}
