package com.ywt.chat.service.cache;

import com.ywt.chat.dao.GroupMemberDao;
import com.ywt.chat.dao.RoomGroupDao;
import com.ywt.chat.domain.entity.RoomGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年05月06日 10:40
 */
@Component
public class GroupMemberCache {
    @Autowired
    private RoomGroupDao roomGroupDao;
    @Autowired
    private GroupMemberDao groupMemberDao;

    /**
     * 获取所有群成员
     *
     * @param roomId
     * @return
     */
    @Cacheable(cacheNames = "member",key = "'groupMember' + #roomId")
    public List<Long> getMemberUidList(Long roomId) {
        RoomGroup roomGroup = roomGroupDao.getByRoomId(roomId);
        if (Objects.isNull(roomGroup)) {
            return null;
        }
        return groupMemberDao.getUidListByRoomGroupId(roomGroup.getId());
    }

    @CacheEvict(cacheNames = "member", key = "'groupMember'+#roomId")
    public List<Long> evictMemberUidList(Long roomId) {
        return null;
    }
}
