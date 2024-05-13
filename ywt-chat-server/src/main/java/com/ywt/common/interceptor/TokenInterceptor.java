package com.ywt.common.interceptor;

import cn.hutool.core.util.ObjUtil;
import com.ywt.common.exception.HttpErrorEnum;
import com.ywt.user.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * 功能描述
 *  登录获取token，验证用户身份拦截器
 * @author: ywt
 * @date: 2024年04月21日 22:41
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String AUTHORIZATION_SCHEMA = "Bearer ";

    public static final String ATTRIBUTE_UID = "uid";

    @Autowired
    private LoginService loginService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = getToken(request);
        Long validUid = loginService.getValidUid(token);
        if (ObjUtil.isNotNull(validUid)) {
            request.setAttribute(ATTRIBUTE_UID,validUid);
        }else {
            // 判断是否是公共的接口
            boolean isPublicURI = isPublicURI(request.getRequestURI());
            //又没有登录态，又不是公开路径，直接401
            if (!isPublicURI) {
                HttpErrorEnum.ACCESS_DENIED.sendHttpError(response);
                return false;
            }
        }
        return true;
    }

    private boolean isPublicURI(String requestURI) {
        int aPublic = requestURI.indexOf("public");
        return aPublic != -1;
    }
    private String getToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        return Optional.ofNullable(header)
                .filter(h -> h.startsWith(AUTHORIZATION_SCHEMA))
                .map(h -> h.substring(AUTHORIZATION_SCHEMA.length()))
                .orElse(null);
    }



}
