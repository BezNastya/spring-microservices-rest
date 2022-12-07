package com.example.bookmodule.repository;

import com.example.bookmodule.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import java.util.List;

@RestController
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByNameAndAuthorId(String name, long aLong);
    Optional<Book> findByName(String name);
    List<Book> findAllByAuthorId(long authorId);
    void deleteAllByAuthorId(long authorId);
}
