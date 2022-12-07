package com.example.authormodule;


import com.example.authormodule.dto.AuthorDto;
import com.example.authormodule.dto.Book;
import com.example.authormodule.entities.Author;
import com.example.authormodule.services.AuthorRepository;
import com.example.authormodule.services.AuthorService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(value = "/author")
public class AsyncController {

    private AuthorRepository authorRepository;
    private AuthorService authorService;
    private MeterRegistry meterRegistry;
    private final Timer timer;

    @Autowired
    public AsyncController(AuthorRepository authorRepository, AuthorService authorService, MeterRegistry meterRegistry) {
        this.authorRepository = authorRepository;
        this.authorService = authorService;
        this.meterRegistry = meterRegistry;
        this.timer = meterRegistry.timer("async.execution", "module", "book-module");
    }

    @GetMapping("/")
    public String test() {
        return "Hello I'm author utility!";
    }

    @GetMapping("/all")
    public List<Author> getAuthors() {
        return authorRepository.findAll();
    }

    @GetMapping("/{id}")
    public Author getById(@PathVariable long id) {
        return authorRepository.findById(id).get();
    }

    @GetMapping("/withBooks/{id}")
    public AuthorDto getWithBooksId(@RequestHeader("Authorization")String token, @PathVariable String id) throws Exception {
        Long authorId = Long.valueOf(id);
        CompletableFuture<Author> authorTask = authorService.getAuthorById(authorId);
        CompletableFuture<List<Book>> booksTask = authorService.getAuthorsWithBooks(authorId, token);

        timer.record(() -> CompletableFuture.allOf(authorTask, booksTask).join());
        Author author = timer.recordCallable(authorTask::get);

        AuthorDto result = new AuthorDto();
        result.setBookList(timer.recordCallable(booksTask::get));
        result.setId(author.getId());
        result.setFirstname(author.getFirstname());
        result.setLastname(author.getLastname());

        return result;
    }



}
