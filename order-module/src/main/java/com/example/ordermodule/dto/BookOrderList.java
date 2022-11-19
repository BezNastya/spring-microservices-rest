package com.example.ordermodule.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class BookOrderList {

    private List<BookOrderDTO> bookOrders;

    public BookOrderList() {
        bookOrders = new ArrayList<>();
    }

    public BookOrderList(List<BookOrderDTO> bookOrderDTOS) {
        this.bookOrders = bookOrderDTOS;
    }
}
