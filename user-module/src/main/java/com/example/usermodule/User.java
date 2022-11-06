package com.example.usermodule;

import lombok.Data;
import org.springframework.data.annotation.Id;

import javax.persistence.*;


@Entity
@Data
@Table(name = "users")
public class User {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "age")
    private int age;


    public User() {}

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
