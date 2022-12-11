package com.example.authormodule.services;

import com.example.authormodule.config.ActiveMQConfiguration;
import com.example.authormodule.dto.Book;
import com.example.authormodule.dto.BooksList;
import com.example.authormodule.dto.book.module.BookRequestDTO;
import com.example.authormodule.entities.Author;
import com.example.authormodule.feign.BookModuleClient;
import jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import static com.example.authormodule.config.ActiveMQConfiguration.AUTHOR_QUEUE;
import static com.example.authormodule.config.ActiveMQConfiguration.BOOK_QUEUE;


@Service
@Slf4j
public class AuthorService {

    private JmsTemplate jmsTemplate;
    private AuthorRepository authorRepository;
    private JwtTokenProvider jwtTokenProvider;
    private BookModuleClient bookModuleClient;

    @Autowired
    public AuthorService(AuthorRepository authorRepository, JwtTokenProvider jwtTokenProvider,
                         BookModuleClient bookModuleClient, JmsTemplate jmsTemplate) {
        this.authorRepository = authorRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.bookModuleClient = bookModuleClient;
        this.jmsTemplate = jmsTemplate;
    }


    @Async("asyncExecutor")
    public CompletableFuture<List<Book>> getAuthorsWithBooks(Long id, String token) {
        BooksList list = bookModuleClient.getBooksByAuthor(token, id);
        if (list != null && list.getBooks() != null) {
            return CompletableFuture.completedFuture(list.getBooks());
        }
        throw new NoSuchElementException();
    }

    @Async("asyncExecutor")
    public CompletableFuture<Author> getAuthorById(Long id) {
        return CompletableFuture.completedFuture(authorRepository.findById(id).get());
    }

    @JmsListener(destination = "EmpTopic")
    public void receiveTopicTest(String message) throws URISyntaxException {
        log.info("'AuthorService' received message='{}'", message);

        RestTemplate restTemplate = new RestTemplate();
        String body = "{\"configuredLevel\": \""+ message+" \"}";

        HttpHeaders headers=new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity requestEntity =new HttpEntity(body, headers);

        ResponseEntity<String> result = restTemplate.exchange(
                "http://localhost:8011/actuator/loggers/com.example.authormodule",
                HttpMethod.POST, requestEntity, String.class);

    }

}
