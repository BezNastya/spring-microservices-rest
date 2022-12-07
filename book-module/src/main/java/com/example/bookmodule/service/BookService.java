package com.example.bookmodule.service;

import com.example.bookmodule.dto.BookDTO;
import com.example.bookmodule.dto.BookRequestDTO;
import com.example.bookmodule.dto.BooksList;
import com.example.bookmodule.entity.Book;
import com.example.bookmodule.repository.BookRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new NoSuchElementException("No such book is present with id" + id);
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
        if(exist != null && exist.getAuthorId()==bookRequestDTO.getAuthorId() ){
            throw new RuntimeException("Book is already exists.");
        }

        jmsTemplate.convertAndSend(BOOK_QUEUE, bookRequestDTO);
        numberOfBooksAdded.increment();
    }
    @JmsListener(destination = BOOK_WITH_AUTHOR_QUEUE)
    public void addBookWithAuthor(BookRequestDTO bookRequestDTO) {
        Book entityBook = BookRequestDTO.convertToEntity(bookRequestDTO);
        bookRepository.save(entityBook);

        log.info("New book added: "+entityBook.toString());
    }

    @Transactional
    public void deleteBookWithAuthor(Book b){
        List<Book> booksWithSameAuthor = bookRepository.findAllByAuthorId(b.getAuthorId());
        if(booksWithSameAuthor.size() == 1 && booksWithSameAuthor.contains(b)){
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

}
