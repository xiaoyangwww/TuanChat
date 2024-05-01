package com.ywt.user.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ywt.common.domain.vo.Req.PageBaseReq;
import com.ywt.user.domain.entity.UserApply;
import com.ywt.user.domain.enums.ApplyReadStatusEnum;
import com.ywt.user.domain.enums.ApplyStatusEnum;
import com.ywt.user.domain.enums.ApplyTypeEnum;
import com.ywt.user.mapper.UserApplyMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;

import static com.ywt.user.domain.enums.ApplyReadStatusEnum.READ;
import static com.ywt.user.domain.enums.ApplyReadStatusEnum.UNREAD;
import static com.ywt.user.domain.enums.ApplyStatusEnum.AGREE;
import static com.ywt.user.domain.enums.ApplyTypeEnum.ADD_FRIEND;

/**
 * <p>
 * 用户申请表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-04-29
 */
@Service
public class UserApplyDao extends ServiceImpl<UserApplyMapper, UserApply> {

    public UserApply getFriendApproving(Long uid, Long targetUid) {
        return lambdaQuery().eq(UserApply::getUid, uid)
                .eq(UserApply::getTargetId, targetUid)
                .eq(UserApply::getStatus, ApplyStatusEnum.WAIT_APPROVAL.getCode())
                .eq(UserApply::getType, ADD_FRIEND.getCode())
                .one();

    }

    /**
     * 获取好友未读消息
     * @param targetId
     * @return
     */
    public Integer getUnReadCount(Long targetId) {
        return lambdaQuery().eq(UserApply::getTargetId, targetId)
                .eq(UserApply::getReadStatus, UNREAD.getCode())
                .count();

    }

    public void agree(Long applyId) {
        lambdaUpdate()
                .set(UserApply::getStatus,AGREE.getCode())
                .eq(UserApply::getId,applyId)
                .update();
    }

    public IPage<UserApply> getApplyPage(Long uid, Page page ) {
        return lambdaQuery()
                .eq(UserApply::getUid, uid)
                .eq(UserApply::getType, ADD_FRIEND.getCode())
                .orderByDesc(UserApply::getCreateTime)
                .page(page);

    }

    public void readApples(Long uid, List<Long> applyIds) {
        lambdaUpdate()
                .set(UserApply::getReadStatus,READ.getCode())
                .eq(UserApply::getReadStatus, UNREAD.getCode())
                .eq(UserApply::getTargetId,uid)
                .in(UserApply::getId,applyIds)
                .update();
    }
}
