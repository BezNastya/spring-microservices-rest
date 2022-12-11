package com.example.authormodule.config;


import jwt.JwtConfigurer;
import jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;



@Configuration
@Import(JwtTokenProvider.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;


    @Autowired
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/actuator/info").permitAll()
                .antMatchers("/actuator/health").permitAll()
                .antMatchers("/actuator/metrics/**").permitAll()
                .antMatchers("/actuator/beans").permitAll()
                .antMatchers("/actuator/configprops").permitAll()
                .antMatchers("/actuator/loggers").permitAll()
                .antMatchers("/actuator/env").permitAll()
                .antMatchers("/actuator/httptrace").permitAll()
                .antMatchers("/actuator/conditions").permitAll()
                .antMatchers("/actuator").hasRole("ADMIN")


                .antMatchers("/").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .apply(new JwtConfigurer(jwtTokenProvider));
    }
}