package com.example.authormodule;


import com.example.authormodule.dto.AuthorDto;
import com.example.authormodule.dto.Book;
import com.example.authormodule.entities.Author;
import com.example.authormodule.services.AuthorRepository;
import com.example.authormodule.services.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController("author")
public class AsyncController {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private AuthorService authorService;

    @GetMapping("/")
    public String test() {
        return "Hello I'm author utility!";
    }

    @GetMapping("/all")
    public List<Author> getAuthors() {
        return authorRepository.findAll();
    }

    @GetMapping("/{id}")
    public Author getById(@PathVariable String id) {
        return authorRepository.findById(Long.valueOf(id)).get();
    }

    @GetMapping("/withBooks/{id}")
    public AuthorDto getWithBooksId(@RequestHeader("Authorization")String token, @PathVariable String id) throws ExecutionException, InterruptedException, URISyntaxException {
        Long authorId = Long.valueOf(id);
        CompletableFuture<Author> authorTask = authorService.getAuthorById(authorId);
        CompletableFuture<List<Book>> booksTask = authorService.getAuthorsWithBooks(authorId,token);

        CompletableFuture.allOf(authorTask, booksTask).join();
        Author author = authorTask.get();

        AuthorDto result = new AuthorDto();
        result.setBookList(booksTask.get());
        result.setId(author.getId());
        result.setFirstname(author.getFirstname());
        result.setLastname(author.getLastname());

        return result;
    }



}
