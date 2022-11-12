package com.example.bookmodule;

import com.example.bookmodule.dto.BookRequestDTO;
import com.example.bookmodule.dto.BooksList;
import com.example.bookmodule.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class BookRestController {

    @Autowired
    private BookService bookService;

    @GetMapping("/books")
    public BooksList getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/books/{userId}")
    public BooksList getAllBooksByUser(@PathVariable long userId) {
        return bookService.getAllBooksByUserId(userId);
    }

    @GetMapping("/booksByAuthor/{authorId}")
    public BooksList getAllBooksByAuthor(@PathVariable long authorId) {
        return bookService.getAllBooksByAuthorId(authorId);
    }

    @PostMapping("/books")
    public void addNewBook(@RequestBody BookRequestDTO bookRequestDTO) {
        bookService.addBook(bookRequestDTO);
    }

    @PutMapping("/books/{id}/{userId}")
    public void updateUserOfBook(@PathVariable long id, @PathVariable long userId) {
        bookService.updateUserForBook(id, userId);
    }
}
