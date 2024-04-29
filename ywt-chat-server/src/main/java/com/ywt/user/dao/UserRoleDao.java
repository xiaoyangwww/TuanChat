package com.ywt.user.dao;

import com.ywt.user.domain.entity.UserRole;
import com.ywt.user.mapper.UserRoleMapper;
import com.ywt.user.service.UserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户角色关系表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-04-25
 */
@Service
public class UserRoleDao extends ServiceImpl<UserRoleMapper, UserRole> {

}
