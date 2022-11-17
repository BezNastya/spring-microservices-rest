package com.example.authormodule.services;

import com.example.authormodule.dto.Book;
import com.example.authormodule.dto.BooksList;
import com.example.authormodule.entities.Author;
import com.example.authormodule.entities.Role;
import jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class AuthorService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    @Async("asyncExecutor")
    public CompletableFuture<List<Book>> getAuthorsWithBooks(Long id,String token) throws URISyntaxException {
        HttpHeaders headers = new HttpHeaders();

        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        headers.add("Authorization",token);
        HttpEntity<Long> entity= new HttpEntity<>(id,headers);
        URI uri = new URI("http://localhost:8002/booksByAuthor/"+id);

        BooksList list = restTemplate.exchange(uri, HttpMethod.GET, entity, BooksList.class).getBody();
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
