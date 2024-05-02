package com.ywt.user.dao;

import com.ywt.user.domain.entity.ItemConfig;
import com.ywt.user.mapper.ItemConfigMapper;
import com.ywt.user.service.ItemConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 功能物品配置表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-04-21
 */
@Service
public class ItemConfigDao extends ServiceImpl<ItemConfigMapper, ItemConfig>  {

    /**
     * 根据type获取物品
     * @param type
     * @return
     */
    public List<ItemConfig> getByType(Integer type) {
        return lambdaQuery().eq(ItemConfig::getType, type).list();

    }

}
