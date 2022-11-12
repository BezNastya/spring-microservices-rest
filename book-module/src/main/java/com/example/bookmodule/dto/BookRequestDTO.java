package com.example.bookmodule.dto;

import com.example.bookmodule.entity.Book;
import lombok.Data;

@Data
public class BookRequestDTO {
    private String name;
    private long authorId;
    private long userId;

    public static Book convertToEntity(BookRequestDTO bookRequestDTO) {
        Book book = new Book();
        book.setName(bookRequestDTO.getName());
        book.setAuthorId(bookRequestDTO.getAuthorId());
        book.setUserId(bookRequestDTO.userId);
        return book;
    }
}