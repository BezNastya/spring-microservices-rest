package com.example.authormodule.services;

import com.example.authormodule.dto.Book;
import com.example.authormodule.dto.BooksList;
import com.example.authormodule.entities.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

@Service
public class AuthorService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AuthorRepository authorRepository;

    @Async("asyncExecutor")
    public CompletableFuture<List<Book>> getAuthorsWithBooks(Long id) {
        BooksList list = restTemplate.getForObject("http://localhost:8002/booksByAuthor/" + id, BooksList.class);
        if (list != null && list.getBooks() != null) {
            return CompletableFuture.completedFuture(list.getBooks());
        }
        throw new NoSuchElementException();
    }

    @Async("asyncExecutor")
    public CompletableFuture<List<Book>> getAuthorsWithUsers(Long id) {
        BooksList list = restTemplate.getForObject("http://localhost:8001/booksByUser/" + id, BooksList.class);
        if (list != null && list.getBooks() != null) {
            return CompletableFuture.completedFuture(list.getBooks());
        }
        throw new NoSuchElementException();
    }

    @Async("asyncExecutor")
    public CompletableFuture<Author> getAuthorById(Long id) {
        return CompletableFuture.completedFuture(authorRepository.findById(id).get());
    }


}
