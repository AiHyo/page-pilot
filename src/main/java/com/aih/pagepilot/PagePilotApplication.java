package com.aih.pagepilot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aih.pagepilot.mapper")
public class PagePilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagePilotApplication.class, args);
    }

}
