package com.example.bookmodule.dto;

import com.example.bookmodule.entity.Book;
import lombok.Data;

@Data
public class BookDTO {

    private long id;
    private String name;

    public BookDTO(Book book) {
        this.id = book.getId();
        this.name = book.getName();
    }
}
