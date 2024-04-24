package com.ywt.common.interceptor;

import cn.hutool.extra.servlet.ServletUtil;
import com.ywt.common.domain.dto.RequestInfo;
import com.ywt.common.utils.RequestHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * 功能描述
 *  搜集用户信息拦截器
 * @author: scott
 * @date: 2024年04月21日 22:35
 */
@Component
public class CollectorInterceptor implements HandlerInterceptor {
    public static final String ATTRIBUTE_UID = "uid";
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setUid(Optional.ofNullable(request.getAttribute(ATTRIBUTE_UID)).map(Object::toString).map(Long::parseLong).orElse(null));
        requestInfo.setIp(ServletUtil.getClientIP(request));
        RequestHolder.set(requestInfo);
        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        RequestHolder.remove();
    }
}
