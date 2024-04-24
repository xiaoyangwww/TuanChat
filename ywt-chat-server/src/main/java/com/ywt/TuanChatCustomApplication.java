package com.ywt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author ywt
 * @date 2024/04/15
 */
@SpringBootApplication(scanBasePackages = {"com.ywt"})
@MapperScan({"com.ywt.**.mapper"})
@ServletComponentScan
@EnableAspectJAutoProxy(exposeProxy = true)
public class TuanChatCustomApplication {

    public static void main(String[] args) {
        SpringApplication.run(TuanChatCustomApplication.class,args);
    }

}