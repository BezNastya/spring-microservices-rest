package com.example.bookmodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class BookModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookModuleApplication.class, args);
    }

}