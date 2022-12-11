package com.example.bookmodule.service;

import com.example.bookmodule.dto.BookDTO;
import com.example.bookmodule.dto.BookRequestDTO;
import com.example.bookmodule.dto.BooksList;
import com.example.bookmodule.entity.Book;
import com.example.bookmodule.exception.BookAlreadyExistsException;
import com.example.bookmodule.exception.NoSuchBookException;
import com.example.bookmodule.repository.BookRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.example.bookmodule.config.ActiveMQConfiguration.*;

@Service
@Slf4j
public class BookService {
    @Autowired
    private BookRepository bookRepository;
    private MeterRegistry meterRegistry;
    private Counter numberOfBooksAdded;
    private Counter timesBookNotFound;

    @Autowired
    public BookService(BookRepository bookRepository, MeterRegistry meterRegistry) {
        this.bookRepository = bookRepository;
        this.meterRegistry = meterRegistry;
        this.numberOfBooksAdded = this.meterRegistry.counter("books.actions", "action", "add");
        this.timesBookNotFound = this.meterRegistry.counter("books.exception", "type", "not-found");
    }

    @Autowired
    private JmsTemplate jmsTemplate;

    public BooksList getAllBooks() {
        List<Book> bookList = bookRepository.findAll();
        List<BookDTO> bookDTOS = bookList.stream()
                .map(BookDTO::new)
                .collect(Collectors.toList());
        return new BooksList(bookDTOS);
    }

    public Book getBook(long id) {
        Optional<Book> bookOptional = bookRepository.findById(id);
        if (bookOptional.isPresent()) {
            return bookOptional.get();
        } else {
            timesBookNotFound.increment();
            throw new NoSuchBookException("No such book is present with id" + id);
        }
    }

    public BooksList getAllBooksByAuthorId(long authorId) {
        List<Book> bookList = bookRepository.findAll();
        List<BookDTO> bookDTOS = bookList.stream()
                .filter(x -> x.getAuthorId() == authorId)
                .map(BookDTO::new)
                .collect(Collectors.toList());
        return new BooksList(bookDTOS);
    }

    public BooksList getAllBooksByIds(List<Long> bookIds) {
        List<Book> bookList = bookRepository.findAllById(bookIds);
        List<BookDTO> bookDTOS = bookList.stream()
                .map(BookDTO::new)
                .collect(Collectors.toList());
        return new BooksList(bookDTOS);
    }

    public void addBook(BookRequestDTO bookRequestDTO) {

        Book exist = bookRepository.findByName(
                bookRequestDTO.getName()).orElse(null);
        if (exist != null && exist.getAuthorId() == bookRequestDTO.getAuthorId()) {
            throw new BookAlreadyExistsException("Book already exists.");
        }

        jmsTemplate.convertAndSend(BOOK_QUEUE, bookRequestDTO);
        numberOfBooksAdded.increment();
    }

    @JmsListener(destination = BOOK_WITH_AUTHOR_QUEUE)
    public void addBookWithAuthor(BookRequestDTO bookRequestDTO) {
        Book entityBook = BookRequestDTO.convertToEntity(bookRequestDTO);
        bookRepository.save(entityBook);

        log.info("New book added: " + entityBook.toString());
    }

    @JmsListener(destination = "EmpTopic")
    public void receive1(String message) {
        log.info("'BookService' received message='{}'", message);

        RestTemplate restTemplate = new RestTemplate();
        String body = "{\"configuredLevel\": \""+ message+" \"}";

        HttpHeaders headers=new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity requestEntity =new HttpEntity(body, headers);

        ResponseEntity<String> result = restTemplate.exchange(
                "http://localhost:8012/actuator/loggers/com.example.authormodule",
                HttpMethod.POST, requestEntity, String.class);
    }

    @Transactional
    public void deleteBookWithAuthor(Book b) {
        List<Book> booksWithSameAuthor = bookRepository.findAllByAuthorId(b.getAuthorId());
        if (booksWithSameAuthor.size() == 1 && booksWithSameAuthor.contains(b)) {
            jmsTemplate.convertAndSend(AUTHOR_QUEUE, b.getAuthorId(), message -> {
                message.setJMSType(DELETE_ORDER_JMS_TYPE);
                return message;
            });
        }
        jmsTemplate.convertAndSend(USERS_BOOK_QUEUE, b.getId(), message -> {
            message.setJMSType(DELETE_ORDER_JMS_TYPE);
            return message;
        });
        bookRepository.deleteAllByAuthorId(b.getAuthorId());
    }

    public void deleteAll() {
        bookRepository.deleteAll();
    }

}
