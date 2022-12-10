package com.example.usermodule;


import lombok.Data;

@Data
public class AuthenticationRequestDto {
    private String login;
    private String password;
}