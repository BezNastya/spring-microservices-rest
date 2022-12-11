package com.example.ordermodule;

import com.example.ordermodule.dto.BookOrderList;
import com.example.ordermodule.service.BookOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class BookOrderRestController {

    @Autowired
    private BookOrderService bookOrderService;

    @GetMapping("/all")
    public BookOrderList getAllBooksOrders() {
        return bookOrderService.getAllBookOrders();
    }

    @GetMapping("/{userId}")
    public BookOrderList getAllBookOrdersByUser(@PathVariable long userId) {
        return bookOrderService.getAllBookOrdersByUserId(userId);
    }
}
