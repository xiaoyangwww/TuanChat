package com.ywt.user.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ywt.common.domain.enums.YesOrNoEnum;
import com.ywt.user.domain.entity.ItemConfig;
import com.ywt.user.domain.entity.UserBackpack;
import com.ywt.user.domain.enums.ItemEnum;
import com.ywt.user.domain.enums.ItemTypeEnum;
import com.ywt.user.mapper.UserBackpackMapper;
import com.ywt.user.service.UserBackpackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户背包表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-04-21
 */
@Service
public class UserBackpackDao extends ServiceImpl<UserBackpackMapper, UserBackpack> {

    public Integer getCountByValidItem(Long uid, Long itemId) {
        return lambdaQuery().eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getItemId, itemId)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getCode())
                .count();
    }

    public UserBackpack getModifyCardByValidItem(Long uid, Long itemId) {
        return lambdaQuery().eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getItemId, itemId)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getCode())
                .one();

    }

    /**
     * 使用改名卡
     */
    public boolean useModifyCard(Long uid, Long itemId) {
        return lambdaUpdate().eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getItemId, itemId)
                .set(UserBackpack::getStatus, YesOrNoEnum.YES.getCode())
                .update();
    }

    /**
     * 获取用户背包里拥有的徽章
     */
    public List<UserBackpack> getUserBadges(Long uid, List<ItemConfig> itemConfigList) {
        List<Long> itemList = itemConfigList.stream().map(ItemConfig::getId).collect(Collectors.toList());
        return lambdaQuery().eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getCode())
                .in(UserBackpack::getItemId, itemList)
                .list();
    }

    public UserBackpack getIdempotent(String idempotent) {
        return lambdaQuery().eq(UserBackpack::getIdempotent,idempotent).one();
    }

    public List<UserBackpack> getByItemIds(List<Long> uidList, List<Long> itemIds) {
        return lambdaQuery()
                .in(UserBackpack::getUid, uidList)
                .in(UserBackpack::getItemId, itemIds)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getCode())
                .list();

    }

    public UserBackpack getFirstValidItem(Long uid, Long itemId) {
        LambdaQueryWrapper<UserBackpack> wrapper = new QueryWrapper<UserBackpack>().lambda()
                .eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getItemId, itemId)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getCode())
                .last("limit 1");
        return getOne(wrapper);

    }
}
