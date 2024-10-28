package com.hf.webflux.hfai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hf.webflux.*.mapper")
public class HfaiApplication {
    public static void main(String[] args) {
        SpringApplication.run(HfaiApplication.class, args);
    }

}
