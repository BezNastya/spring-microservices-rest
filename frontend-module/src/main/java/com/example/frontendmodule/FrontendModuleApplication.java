package com.example.frontendmodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class FrontendModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrontendModuleApplication.class, args);
    }

}
