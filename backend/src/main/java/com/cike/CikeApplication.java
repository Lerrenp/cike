package com.cike;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 「此刻」图文分享社区后端启动类
 */
@SpringBootApplication
@MapperScan("com.cike.mapper")
public class CikeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CikeApplication.class, args);
    }
}
