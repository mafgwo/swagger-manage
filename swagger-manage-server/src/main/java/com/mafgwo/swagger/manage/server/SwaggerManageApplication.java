package com.mafgwo.swagger.manage.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SwaggerManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwaggerManageApplication.class, args);
    }

}
