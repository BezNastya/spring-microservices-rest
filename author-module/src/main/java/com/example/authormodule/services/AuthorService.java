package com.example.authormodule.services;

import com.example.authormodule.dto.Book;
import com.example.authormodule.dto.BooksList;
import com.example.authormodule.entities.Author;
import com.example.authormodule.feign.BookModuleClient;
import jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import static com.example.authormodule.config.ActiveMQConfiguration.AUTHOR_QUEUE;

@Service
@Slf4j
public class AuthorService {
    private AuthorRepository authorRepository;
    private JwtTokenProvider jwtTokenProvider;
    private BookModuleClient bookModuleClient;

    @Autowired
    public AuthorService(AuthorRepository authorRepository, JwtTokenProvider jwtTokenProvider,
                         BookModuleClient bookModuleClient) {
        this.authorRepository = authorRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.bookModuleClient = bookModuleClient;
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

    @JmsListener(destination = AUTHOR_QUEUE, selector = "JMSType = 'DELETE'")
    public void deleteAuthor(long authorId) {
        log.info("got something" + authorId);
        authorRepository.deleteById(authorId);
    }


}
