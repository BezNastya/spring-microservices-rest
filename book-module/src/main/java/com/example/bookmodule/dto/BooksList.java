package com.example.bookmodule.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class BooksList {

    private List<BookDTO> books;

    public BooksList() {
        books = new ArrayList<>();
    }

    public BooksList(List<BookDTO> bookDTOS) {
        this.books = bookDTOS;
    }
}
