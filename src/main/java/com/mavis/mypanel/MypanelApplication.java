package com.mavis.mypanel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mavis.mypanel.dao")
public class MypanelApplication {

    public static void main(String[] args) {
        SpringApplication.run(MypanelApplication.class, args);
    }

}
