package com.example.authormodule.dto;

import com.example.authormodule.dto.Book;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BooksList {

    private List<Book> books;

    public BooksList() {
        books = new ArrayList<>();
    }
}
