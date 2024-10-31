package com.hf.webflux.hfai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.hf.webflux.*.mapper")
@EnableScheduling
public class HfaiApplication {
    public static void main(String[] args) {
        SpringApplication.run(HfaiApplication.class, args);
    }

}
