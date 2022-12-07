package com.example.authormodule.services;

import com.example.authormodule.dto.Book;
import com.example.authormodule.dto.BooksList;
import com.example.authormodule.entities.Author;
import com.example.authormodule.entities.Role;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import static com.example.authormodule.config.ActiveMQConfiguration.BOOK_QUEUE;

import static com.example.bookmodule.config.ActiveMQConfiguration.AUTHOR_QUEUE;

@Service
@Slf4j
public class AuthorService {

    private JmsTemplate jmsTemplate;
    private RestTemplate restTemplate;
    private AuthorRepository authorRepository;
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthorService(JmsTemplate jmsTemplate, RestTemplate restTemplate, AuthorRepository authorRepository, JwtTokenProvider jwtTokenProvider) {
        this.restTemplate = restTemplate;
        this.authorRepository = authorRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jmsTemplate = jmsTemplate;
    }


    @Async("asyncExecutor")
    public CompletableFuture<List<Book>> getAuthorsWithBooks(Long id, String token) throws URISyntaxException {
        HttpHeaders headers = new HttpHeaders();

        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        headers.add("Authorization", token);
        HttpEntity<Long> entity = new HttpEntity<>(id, headers);
        URI uri = new URI("http://localhost:8002/booksByAuthor/" + id);

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
