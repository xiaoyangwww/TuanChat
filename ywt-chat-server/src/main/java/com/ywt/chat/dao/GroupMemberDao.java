package com.ywt.chat.dao;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ywt.chat.domain.entity.GroupMember;
import com.ywt.chat.domain.enums.GroupRoleEnum;
import com.ywt.chat.mapper.GroupMemberMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywt.chat.service.cache.GroupMemberCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ywt.chat.domain.enums.GroupRoleEnum.ADMIN_LIST;

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

    @Autowired
    @Lazy
    private GroupMemberCache groupMemberCache;

    public GroupMember getMember(Long groupId, Long uid) {
        return lambdaQuery().eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUid, uid)
                .one();

    }

    /**
     * 获取群成员
     * @param groupId
     * @return
     */
    public List<Long> getUidListByRoomGroupId(Long groupId) {
        List<GroupMember> list = lambdaQuery()
                .eq(GroupMember::getGroupId, groupId)
                .select(GroupMember::getUid)
                .list();
        return list.stream().map(GroupMember::getUid).collect(Collectors.toList());
    }

    /**
     * 是否是群主
     *
     * @param id  群组ID
     * @param uid 用户ID
     * @return 是否是群主
     */
    public Boolean isLord(Long id, Long uid) {
        GroupMember member = lambdaQuery()
                .eq(GroupMember::getUid, uid)
                .eq(GroupMember::getGroupId, id)
                .eq(GroupMember::getRole, GroupRoleEnum.LEADER.getType())
                .one();
        return ObjectUtil.isNotNull(member);
    }


    /**
     * 是否是管理员
     *
     * @param id  群组ID
     * @param uid 用户ID
     * @return 是否是管理员
     */
    public boolean isManager(Long id, Long uid) {
        GroupMember groupMember = this.lambdaQuery()
                .eq(GroupMember::getGroupId, id)
                .eq(GroupMember::getUid, uid)
                .eq(GroupMember::getRole, GroupRoleEnum.MANAGER.getType())
                .one();
        return ObjectUtil.isNotNull(groupMember);
    }

    /**
     * 判断用户是否在房间中
     *
     * @param roomId  房间ID
     * @param uidList 用户ID
     * @return 是否在群聊中
     */
    public Boolean isGroupShip(Long roomId, List<Long> uidList) {
        List<Long> memberUidList = groupMemberCache.getMemberUidList(roomId);
        return new HashSet<>(memberUidList).containsAll(uidList);
    }

    /**
     * 根据群组ID删除群成员
     *
     * @param groupId 群组ID
     * @param uidList 群成员列表
     * @return 是否删除成功
     */
    public Boolean removeByGroupId(Long groupId,List<Long> uidList) {
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        if (uidList.isEmpty()) {
            wrapper.eq(GroupMember::getGroupId,groupId);
        }else {
            wrapper.eq(GroupMember::getGroupId,groupId)
                    .in(GroupMember::getUid,uidList);
        }
        return remove(wrapper);
    }

    public List<GroupMember> getSelfGroup(Long uid) {
        return lambdaQuery()
                .eq(GroupMember::getUid,uid)
                .eq(GroupMember::getRole, GroupRoleEnum.LEADER.getType())
                .list();
    }

    public List<Long> getMemberBatch(Long groupId, List<Long> uidList) {
        return lambdaQuery()
                .eq(GroupMember::getGroupId, groupId)
                .in(GroupMember::getUid, uidList)
                .select(GroupMember::getUid)
                .list()
                .stream().map(GroupMember::getUid).collect(Collectors.toList());
    }

    /**
     * 查询现有管理员
     * @param groupId
     * @return
     */
    public List<Long> getManageUidList(Long groupId) {
        return lambdaQuery()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getRole, GroupRoleEnum.MANAGER.getType())
                .select(GroupMember::getUid)
                .list()
                .stream().map(GroupMember::getUid).collect(Collectors.toList());
    }

    /**
     * 增加管理员
     * @param groupId
     * @param uidList
     */
    public void addAdmin(Long groupId, List<Long> uidList) {
        lambdaUpdate()
                .eq(GroupMember::getGroupId,groupId)
                .in(GroupMember::getUid,uidList)
                .set(GroupMember::getRole,GroupRoleEnum.MANAGER.getType())
                .update();
    }

    /**
     * 撤销管理员
     * @param groupId
     * @param uidList
     */
    public void revokeAdmin(Long groupId, List<Long> uidList) {
        lambdaUpdate()
                .eq(GroupMember::getGroupId,groupId)
                .in(GroupMember::getUid,uidList)
                .set(GroupMember::getRole,GroupRoleEnum.MEMBER.getType())
                .update();
    }

    /**
     * 获取群成员在群中的角色
     * @param groupId
     * @param uidList
     * @return
     */
    public Map<Long, Integer> getMemberMapRole(Long groupId, List<Long> uidList) {
        return lambdaQuery()
                .eq(GroupMember::getGroupId, groupId)
                .in(GroupMember::getUid, uidList)
                .in(GroupMember::getRole, ADMIN_LIST)
                .select(GroupMember::getUid, GroupMember::getRole)
                .list()
                .stream().collect(Collectors.toMap(GroupMember::getUid, GroupMember::getRole));
    }
}
