package com.example.adminmodule;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAdminServer
@SpringBootApplication
public class AdminModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminModuleApplication.class, args);
    }

}
