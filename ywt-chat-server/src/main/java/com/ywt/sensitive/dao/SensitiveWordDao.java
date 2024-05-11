package com.ywt.sensitive.dao;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywt.sensitive.domain.SensitiveWord;
import com.ywt.sensitive.mapper.SensitiveWordMapper;
import org.springframework.stereotype.Service;

/**
 * 敏感词DAO
 *
 * @author zhaoyuhang
 * @since 2023/06/11
 */
@Service
public class SensitiveWordDao extends ServiceImpl<SensitiveWordMapper, SensitiveWord> {

}
