package com.ywt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * @author ywt
 * @date 2024/04/15
 */
@SpringBootApplication(scanBasePackages = {"com.ywt"})
//@MapperScan({"com.ywt.**.mapper"})
@ServletComponentScan
public class TuanChatCustomApplication {

    public static void main(String[] args) {
        SpringApplication.run(TuanChatCustomApplication.class,args);
    }

}