package com.example.authormodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@SpringBootApplication
public class AuthorDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorDemoApplication.class, args);
    }

    @Bean
    RestTemplate restTemplate(){
        ProtobufHttpMessageConverter conv = new  ProtobufHttpMessageConverter();
        MappingJackson2HttpMessageConverter mapConv = new MappingJackson2HttpMessageConverter();
        return new RestTemplate(List.of(conv,mapConv));
    }

    @Bean
    ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufHttpMessageConverter();
    }
}
