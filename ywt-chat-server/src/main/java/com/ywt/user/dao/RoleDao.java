package com.ywt.user.dao;

import com.ywt.user.domain.entity.Role;
import com.ywt.user.mapper.RoleMapper;
import com.ywt.user.service.RoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-04-25
 */
@Service
public class RoleDao extends ServiceImpl<RoleMapper, Role> {

}
