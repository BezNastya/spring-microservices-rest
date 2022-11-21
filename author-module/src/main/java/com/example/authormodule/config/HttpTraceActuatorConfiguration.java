package com.example.authormodule.config;

import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//this class was added to make httptrace work
//httptrace - actuator endpoint that displays HTTP trace information (by default, the last 100 HTTP request-response exchanges).
@Configuration
public class HttpTraceActuatorConfiguration {

    @Bean
    public HttpTraceRepository httpTraceRepository() {
        return new InMemoryHttpTraceRepository();
    }

}