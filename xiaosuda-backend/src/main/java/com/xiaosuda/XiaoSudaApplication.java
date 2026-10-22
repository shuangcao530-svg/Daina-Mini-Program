package com.xiaosuda;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xiaosuda.mapper")
public class XiaoSudaApplication {
    public static void main(String[] args) {
        SpringApplication.run(XiaoSudaApplication.class, args);
    }
}
