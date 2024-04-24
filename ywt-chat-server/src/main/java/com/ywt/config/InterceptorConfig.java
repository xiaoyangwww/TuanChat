package com.ywt.config;

import com.ywt.common.interceptor.CollectorInterceptor;
import com.ywt.common.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 功能描述
 *
 * @author: scott
 * @date: 2024年04月21日 23:17
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Autowired
    private CollectorInterceptor collectorInterceptor;



    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/capi/**");
        registry.addInterceptor(collectorInterceptor).addPathPatterns("/capi/**");
    }
}
