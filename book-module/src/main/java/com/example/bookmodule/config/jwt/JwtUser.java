package com.example.bookmodule.config.jwt;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;



public class JwtUser implements UserDetails {

    private final Long id;
    private final String login;
    private final String password;
    private final int age;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtUser(
            Long id,
            String login,
            int age,
            String password,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.login=login;
        this.age=age;
        this.password = password;
        this.authorities = authorities;

    }


    @JsonIgnore
    public Long getId() {
        return id;
    }



    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.login;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

}