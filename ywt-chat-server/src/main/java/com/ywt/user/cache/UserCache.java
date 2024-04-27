package com.ywt.user.cache;

import com.ywt.user.dao.BlackDao;
import com.ywt.user.dao.ItemConfigDao;
import com.ywt.user.dao.RoleDao;
import com.ywt.user.dao.UserRoleDao;
import com.ywt.user.domain.entity.Black;
import com.ywt.user.domain.entity.ItemConfig;
import com.ywt.user.domain.entity.UserRole;
import com.ywt.user.domain.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月27日 10:43
 */
@Component
public class UserCache {

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private BlackDao blackDao;

    @Cacheable(cacheNames = "user",key = "'roles' + #uid")
    public Set<Long> getRoleSet(Long uid) {
        List<UserRole> list = userRoleDao.lambdaQuery().eq(UserRole::getUid, uid).list();
        return list.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
    }

    @Cacheable(cacheNames = "user", key = "'blackList'")
    public Map<Integer, Set<String>> getBlackMap() {
        Map<Integer, List<Black>> collect = blackDao.list().stream().collect(Collectors.groupingBy(Black::getType));
        Map<Integer, Set<String>> result = new HashMap<>(collect.size());
        for (Map.Entry<Integer, List<Black>> entry : collect.entrySet()) {
            result.put(entry.getKey(), entry.getValue().stream().map(Black::getTarget).collect(Collectors.toSet()));
        }
        return result;
    }

    @CacheEvict(cacheNames = "user", key = "'blackList'")
    public Map<Integer, Set<String>> evictBlackMap() {
        return null;
    }




}
