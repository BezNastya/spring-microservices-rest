package com.example.authormodule.dto.book.module;
import com.example.authormodule.dto.book.module.Book;
import lombok.Data;

@Data
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
