package com.example.fullstack;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.fullstack.mapper")
@SpringBootApplication
public class FullstackBasicApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackBasicApplication.class, args);
    }
}
