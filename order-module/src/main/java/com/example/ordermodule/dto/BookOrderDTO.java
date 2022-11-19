package com.example.ordermodule.dto;

import com.example.ordermodule.entity.BookOrder;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class BookOrderDTO {
    private long bookId;
    private long userId;

    public BookOrderDTO(BookOrder bookOrder) {
        this.bookId = bookOrder.getBookId();
        this.userId = bookOrder.getUserId();
    }

    public static BookOrder convertToEntity(BookOrderDTO bookOrderDTO) {
        BookOrder bookOrder = new BookOrder();
        bookOrder.setBookId(bookOrderDTO.getBookId());
        bookOrder.setUserId(bookOrderDTO.getUserId());
        return bookOrder;
    }
}
