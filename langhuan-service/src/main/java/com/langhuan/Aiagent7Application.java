package com.langhuan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class Aiagent7Application {

    public static void main(String[] args) {
        SpringApplication.run(Aiagent7Application.class, args);
        System.out.println("启动成功！");
    }

}
