package com.example.usermodule;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserDto  implements Serializable {

    private long id;

    private String login;

    private int age;

    private List<Book> books = new ArrayList<>();
}
