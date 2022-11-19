package com.example.bookmodule.startData;


import com.example.bookmodule.entity.Book;
import com.example.bookmodule.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class StartupData implements CommandLineRunner {


    private final BookRepository bookRepository;


    private static final Logger logger = LoggerFactory.getLogger(StartupData.class);

    @Autowired
    public StartupData(BookRepository bookRepository) {
        this.bookRepository = bookRepository;

    }

    @Override
    public void run(String... args) {
        createBooks();
    }



    private void createBooks() {
        Book book1 = new Book();
        book1.setName("bool1");
        book1.setId(1);
        book1.setAuthorId(1);
        Book book2 = new Book();
        book2.setName("bool2");
        book2.setId(2);

        bookRepository.save(book1);
        bookRepository.save(book2);


    }




    }

