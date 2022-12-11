package com.example.bookmodule;

import com.example.book.module.BookProto;
import com.example.bookmodule.dto.BookFullDTO;
import com.example.bookmodule.dto.BookRequestDTO;
import com.example.bookmodule.dto.BooksList;
import com.example.bookmodule.entity.Book;
import com.example.bookmodule.service.BookOrderService;
import com.example.bookmodule.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@Slf4j
public class BookRestController {

    @Autowired
    private BookService bookService;
    @Autowired
    private BookOrderService bookOrderService;

    @GetMapping("/all")
    public BooksList getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{userId}")
    public BooksList getAllBooksByUser(@PathVariable long userId) {
        List<Long> bookIds = bookOrderService.getAllBooksInOrdersByUserId(userId);
        return bookService.getAllBooksByIds(bookIds);
    }

    @RequestMapping(value = "/{id}")
    public BookProto.Book getBook(@PathVariable long id) {
        BookProto.Book res = bookService.getProtoBook(id);
        System.out.println("SEND PROTO: "+ res);
        return res;
    }

    @GetMapping("/full/{id}")
    public BookFullDTO getFullBook(@PathVariable long id){
        return bookService.getBookWithAuthor(id);
    }

    @GetMapping("/booksByAuthor/{authorId}")
    public BooksList getAllBooksByAuthor(@PathVariable long authorId) {
        return bookService.getAllBooksByAuthorId(authorId);
    }

    @PostMapping("/add")
    public void addNewBook(@RequestBody BookRequestDTO bookRequestDTO) {
        bookService.addBook(bookRequestDTO);
    }

    @PutMapping("/order/{id}/{userId}")
    public void createRequestForBook(@PathVariable long id, @PathVariable long userId) {
        bookOrderService.createOrder(id, userId);
    }

    @PutMapping("/cancel/{id}/{userId}")
    public void cancelRequestForBook(@PathVariable long id, @PathVariable long userId) {
        bookOrderService.cancelOrder(id, userId);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteBook(@PathVariable long id) {
        bookService.deleteBook(id);
        return "Deleted " + id;
    }
}
