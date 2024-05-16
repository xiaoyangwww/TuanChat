package com.ywt.user.cache;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.api.R;
import com.ywt.common.constant.RedisKey;
import com.ywt.user.dao.*;
import com.ywt.user.domain.entity.Black;
import com.ywt.user.domain.entity.ItemConfig;
import com.ywt.user.domain.entity.User;
import com.ywt.user.domain.entity.UserRole;
import com.ywt.user.domain.enums.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import ywt.chat.common.utils.RedisUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月27日 10:43
 */
@Component
public class UserCache {

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private BlackDao blackDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserSummaryCache userSummaryCache;

    /**
     * 获取用户信息，盘路缓存模式
     */
    public User getUserInfo(Long uid) {//todo 后期做二级缓存
        return getUserInfoBatch(Collections.singleton(uid)).get(uid);
    }

    /**
     * 获取用户信息，盘路缓存模式
     */
    public Map<Long, User> getUserInfoBatch(Set<Long> uids) {
        //批量组装key
        List<String> keys = uids.stream().map(a -> RedisKey.getKey(RedisKey.USER_INFO_STRING, a)).collect(Collectors.toList());
        //批量get
        List<User> mget = RedisUtils.mget(keys, User.class);
        Map<Long, User> map = mget.stream().filter(Objects::nonNull).collect(Collectors.toMap(User::getId, Function.identity()));
        //发现差集——还需要load更新的uid
        List<Long> needLoadUidList = uids.stream().filter(a -> !map.containsKey(a)).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(needLoadUidList)) {
            //批量load
            List<User> needLoadUserList = userDao.listByIds(needLoadUidList);
            Map<String, User> redisMap = needLoadUserList.stream().collect(Collectors.toMap(a -> RedisKey.getKey(RedisKey.USER_INFO_STRING, a.getId()), Function.identity()));
            RedisUtils.mset(redisMap, 5 * 60);
            //加载回redis
            map.putAll(needLoadUserList.stream().collect(Collectors.toMap(User::getId, Function.identity())));
        }
        return map;
    }

    public void userInfoChange(Long uid) {
        delUserInfo(uid);
        //删除UserSummaryCache，前端下次懒加载的时候可以获取到最新的数据
        userSummaryCache.delete(uid);
        refreshUserModifyTime(uid);
    }

    private void refreshUserModifyTime(Long uid) {
        String key = RedisKey.getKey(RedisKey.USER_MODIFY_STRING,uid);
        RedisUtils.set(key, new Date().getTime());
    }

    public void delUserInfo(Long uid) {
        String key = RedisKey.getKey(RedisKey.USER_INFO_STRING, uid);
        RedisUtils.del(key);
    }



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


    public List<Long> getRedisLastModifyTimeList(List<Long> uids) {
        List<String> keys = uids.stream().map(id -> RedisKey.getKey(RedisKey.USER_MODIFY_STRING,id)).collect(Collectors.toList());
        return RedisUtils.mget(keys, Long.class);
    }

    /**
     * 用户下线
     */
    public void offline(Long uid, Date lastOptTime) {
        // 清除上线表
        RedisUtils.zRemove(RedisKey.getKey(RedisKey.ONLINE_UID_ZET),uid);
        // 更新下线表
        RedisUtils.zAdd(RedisKey.getKey(RedisKey.OFFLINE_UID_ZET),uid,lastOptTime.getTime());

    }

    /**
     * 用户上线
     */
    public void online(Long uid, Date lastOptTime) {
        // 清除下线表
        RedisUtils.zRemove(RedisKey.getKey(RedisKey.OFFLINE_UID_ZET),uid);
        // 更新上线表
        RedisUtils.zAdd(RedisKey.getKey(RedisKey.ONLINE_UID_ZET),uid,lastOptTime.getTime());
    }

    /**
     * 获取上线用户人数
     */
    public Long getOnlineCount() {
        return RedisUtils.zCard(RedisKey.getKey(RedisKey.ONLINE_UID_ZET));
    }

    /**
     * 判断用户是否在上线表中
     * @param uid
     */
    public boolean isOnline(Long uid) {
        return RedisUtils.zIsMember(RedisKey.getKey(RedisKey.ONLINE_UID_ZET),uid);
    }

    public void remove(Long uid) {
        // 清除上线表
        RedisUtils.zRemove(RedisKey.getKey(RedisKey.ONLINE_UID_ZET),uid);
        // 清除下线表
        RedisUtils.zRemove(RedisKey.getKey(RedisKey.OFFLINE_UID_ZET),uid);
    }
}
