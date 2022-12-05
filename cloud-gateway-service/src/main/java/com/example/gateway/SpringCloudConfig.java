package com.example.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringCloudConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/books/**")
                        .uri("lb://book-module")
                        .id("book-module"))

                .route(r -> r.path("/users/**")
                        .uri("lb://user-module")
                        .id("user-module"))
                .route(r -> r.path("/authors/**")
                        .uri("lb://author-module")
                        .id("author-module"))
                .build();
    }

}