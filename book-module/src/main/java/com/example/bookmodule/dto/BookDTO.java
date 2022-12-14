package com.example.bookmodule.dto;

import com.example.bookmodule.entity.Book;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class BookDTO {

    private long id;
    private String name;

    public BookDTO(Book book) {
        this.id = book.getId();
        this.name = book.getName();
    }
}
