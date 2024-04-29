package com.ywt.user.dao;

import com.ywt.user.domain.entity.Black;
import com.ywt.user.mapper.BlackMapper;
import com.ywt.user.service.BlackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 黑名单 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-04-25
 */
@Service
public class BlackDao extends ServiceImpl<BlackMapper, Black> {

}
