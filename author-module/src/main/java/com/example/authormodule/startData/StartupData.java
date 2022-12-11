package com.example.authormodule.startData;


import com.example.authormodule.entities.Author;
import com.example.authormodule.services.AuthorRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class StartupData implements CommandLineRunner {


    private final AuthorRepository authorRepository;

    private static final Logger logger = LoggerFactory.getLogger(StartupData.class);

    @Autowired
    public StartupData(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public void run(String... args) {
        authors();
    }



    private void authors() {
        Author author = new Author();
        author.setId(1L);
        author.setFirstname("fd");
        author.setLastname("sad");
        Author author2 = new Author();
        author2.setFirstname("fd");
        author2.setLastname("sad");
        author2.setId(2L);

        authorRepository.save(author);
        authorRepository.save(author2);


    }

}