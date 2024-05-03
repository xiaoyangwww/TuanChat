package com.ywt.chat.dao;

import com.ywt.chat.domain.entity.GroupMember;
import com.ywt.chat.mapper.GroupMemberMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 群成员表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-05-02
 */
@Service
public class GroupMemberDao extends ServiceImpl<GroupMemberMapper, GroupMember>  {

    public GroupMember getMember(Long groupId, Long uid) {
        return lambdaQuery().eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUid, uid)
                .one();

    }
}
