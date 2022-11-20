package com.example.ordermodule.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserDto  implements Serializable {

    private long id;

    private String login;

    private int age;

    private List<BookDTO> bookDTOS = new ArrayList<>();
}
