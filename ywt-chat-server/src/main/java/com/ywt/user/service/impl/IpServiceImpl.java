package com.ywt.user.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.ywt.common.handler.GlobalUncaughtExceptionHandler;
import com.ywt.user.cache.UserCache;
import com.ywt.user.dao.UserDao;
import com.ywt.user.domain.dto.IpResult;
import com.ywt.user.domain.entity.IpDetail;
import com.ywt.user.domain.entity.IpInfo;
import com.ywt.user.domain.entity.User;
import com.ywt.user.service.IpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月25日 11:56
 */
@Service
@Slf4j
public class IpServiceImpl implements IpService, DisposableBean {

    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(500),
            new NamedThreadFactory("refresh-ipDetail", null, false,
                    GlobalUncaughtExceptionHandler.getInstance()));
    public static final String URL = "https://ip.taobao.com/outGetIpInfo?ip=%s&accessKey=alibaba-inc";

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserCache userCache;


    @Override
    public void refreshIpDetailAsync(Long uid) {
        EXECUTOR.execute(() -> {
            User user = userDao.getById(uid);
            IpInfo ipInfo = user.getIpInfo();
            if (Objects.isNull(ipInfo)) {
                return;
            }
            String ip = ipInfo.needRefreshIp();
            if (StrUtil.isEmpty(ip)) {
                return;
            }
            IpDetail ipDetail = tryGetIpDetailOrNullTreeTimes(ip);
            if (ObjectUtil.isNull(ipDetail)){
                log.error("get ip detail fail ip:{},uid:{}", ip, uid);
                return;
            }
            ipInfo.updateIpDetail(ipDetail);
            User update = new User();
            update.setId(uid);
            update.setIpInfo(ipInfo);
            userDao.updateById(update);
            //删除缓存
            userCache.userInfoChange(uid);
        });

    }

    private static IpDetail tryGetIpDetailOrNullTreeTimes(String ip) {
        for (int i = 0; i < 3; i++) {
            IpDetail ipDetail = tryGetIpDetail(ip);
            if (ObjectUtil.isNotNull(ipDetail)) {
                return ipDetail;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private static IpDetail tryGetIpDetail(String ip) {
        String uri = String.format(URL, ip);
        try {
            String data = HttpUtil.get(uri);
            IpResult<IpDetail> ipResult = JSONUtil.toBean(data, new TypeReference<IpResult<IpDetail>>() {
            }, false);
            if (ipResult.isSuccess()) {
                return ipResult.getData();
            }
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
        return null;
    }

    //测试耗时结果 100次查询总耗时约100s，平均一次成功查询需要1s,可以接受
    //第99次成功,目前耗时：99545ms
//    public static void main(String[] args) {
//        Date begin = new Date();
//        for (int i = 0; i < 100; i++) {
//            int finalI = i;
//            EXECUTOR.execute(() -> {
//                IpDetail ipDetail = tryGetIpDetailOrNullTreeTimes("113.90.36.126");
//                if (Objects.nonNull(ipDetail)) {
//                    Date date = new Date();
//                    System.out.println(String.format("第%d次成功,目前耗时：%dms", finalI, (date.getTime() - begin.getTime())));
//                }
//            });
//        }
//    }


    /**
     * 这段代码是一个典型的在销毁对象时关闭线程池的方法。
     *
     * EXECUTOR.shutdown(): 这一行代码调用了线程池的 shutdown() 方法，它向线程池发送一个关闭请求，使得线程池不再接受新的任务，
     * 但会等待已经提交的任务执行完毕。
     * EXECUTOR.awaitTermination(30, TimeUnit.SECONDS): 这一行代码调用了 awaitTermination() 方法，它会等待线程池终止。
     * 参数 30 表示最多等待 30 秒，
     * TimeUnit.SECONDS 指定了时间单位。这里的意思是，等待线程池中的任务执行完毕，或者超过 30 秒后停止等待。
     * 如果在指定时间内线程池没有终止，会打印错误日志，提示线程池关闭超时。
     * @throws InterruptedException
     */
    @Override
    public void destroy() throws InterruptedException {
        EXECUTOR.shutdown();
        if (!EXECUTOR.awaitTermination(30, TimeUnit.SECONDS)) {//最多等30秒，处理不完就拉倒
            if (log.isErrorEnabled()) {
                log.error("Timed out while waiting for executor [{}] to terminate", EXECUTOR);
            }
        }
    }
}
