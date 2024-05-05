package com.ywt.chat.transaction.dao;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.ywt.chat.transaction.domain.entity.SecureInvokeRecord;
import com.ywt.chat.transaction.mapper.SecureInvokeRecordMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywt.chat.transaction.service.SecureInvokeService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 本地消息表 服务实现类
 * </p>
 *
 * @author ywt
 * @since 2024-05-04
 */
@Service
public class SecureInvokeRecordDao extends ServiceImpl<SecureInvokeRecordMapper, SecureInvokeRecord> {


    public List<SecureInvokeRecord> getWaitRetryRecords() {
        //查2分钟前的失败数据。避免刚入库的数据被查出来
        DateTime afterTime = DateUtil.offsetMinute(new Date(), -(int) SecureInvokeService.RETRY_INTERVAL_MINUTES);
        return lambdaQuery()
                .eq(SecureInvokeRecord::getStatus, SecureInvokeRecord.STATUS_WAIT)
                .lt(SecureInvokeRecord::getNextRetryTime, new Date())
                .lt(SecureInvokeRecord::getCreateTime, afterTime)
                .list();
    }
}
