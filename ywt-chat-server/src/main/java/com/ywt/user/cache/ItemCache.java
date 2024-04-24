package com.ywt.user.cache;

import com.ywt.user.dao.ItemConfigDao;
import com.ywt.user.domain.entity.ItemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 功能描述
 *  物品本地缓存
 * @author: scott
 * @date: 2024年04月23日 11:40
 */
@Component
public class ItemCache {
    @Autowired
    private ItemConfigDao itemConfigDao;

    @Cacheable(cacheNames = "item",key = "'itemsByType:' + #type")
    public List<ItemConfig> getByType(Integer type) {
        return itemConfigDao.getByType(type);
    }

    @Cacheable(cacheNames = "item",key = "'itemsById:' + #itemId")
    public ItemConfig getByType(Long itemId ) {
        return itemConfigDao.getById(itemId);
    }
}
