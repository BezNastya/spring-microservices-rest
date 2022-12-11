package com.example.bookmodule.dto;

import com.example.bookmodule.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookRequestDTO {
    private String name;

    private long authorId;

    private String firstname;
    private String lastname;

    public static Book convertToEntity(BookRequestDTO bookRequestDTO){
        Book book = new Book();
        book.setName(bookRequestDTO.getName());
        book.setAuthorId(bookRequestDTO.getAuthorId());
        return book;
    }
}
