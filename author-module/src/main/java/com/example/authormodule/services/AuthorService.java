package com.example.authormodule.services;

import com.example.authormodule.dto.Book;
import com.example.authormodule.dto.BooksList;
import com.example.authormodule.entities.Author;
import com.example.authormodule.feign.BookModuleClient;
import jwt.JwtTokenProvider;
import com.example.bookmodule.config.ActiveMQConfiguration;
import com.example.bookmodule.dto.BookRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
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

import static com.example.authormodule.config.ActiveMQConfiguration.BOOK_QUEUE;

import static com.example.bookmodule.config.ActiveMQConfiguration.AUTHOR_QUEUE;

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

    @JmsListener(destination = BOOK_QUEUE)
    public void addBookWithAuthor(BookRequestDTO bookRequestDTO) {

        Author exist =
                authorRepository.findById(bookRequestDTO.getAuthorId()).orElse(null);
        if(exist == null){
            Author newAuth = new Author();
            newAuth.setFirstname(bookRequestDTO.getFirstname());
            newAuth.setLastname(bookRequestDTO.getLastname());

            authorRepository.save(newAuth);
            Author saved = authorRepository
                    .findByFirstnameAndLastname(newAuth.getFirstname(), newAuth.getLastname()).orElse(null);
            bookRequestDTO.setAuthorId(saved.getId());
        }

        com.example.bookmodule.entity.Book entityBook
                = BookRequestDTO.convertToEntity(bookRequestDTO);
        log.info("Book to add:" + entityBook);

        jmsTemplate.convertAndSend(ActiveMQConfiguration.BOOK_WITH_AUTHOR_QUEUE, bookRequestDTO, message -> {
                message.setJMSType("Test");
            return message;
        });

    }

    @JmsListener(destination = AUTHOR_QUEUE, selector = "JMSType = 'DELETE'")
    public void deleteAuthor(long authorId) {
        log.info("got something" + authorId);
        authorRepository.deleteById(authorId);
    }


}
