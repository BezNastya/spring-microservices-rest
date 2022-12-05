package com.example.bookmodule.service;

import com.example.bookmodule.dto.BookDTO;
import com.example.bookmodule.dto.BookRequestDTO;
import com.example.bookmodule.dto.BooksList;
import com.example.bookmodule.entity.Book;
import com.example.bookmodule.repository.BookRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
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
    private BookOrderService bookOrderService;

    @Autowired
    private JmsTemplate jmsTemplate;

    public BooksList getAllBooks() {
        List<Book> bookList = bookRepository.findAll();
        List<BookDTO> bookDTOS = bookList.stream()
                .map(BookDTO::new)
                .collect(Collectors.toList());
        return new BooksList(bookDTOS);
    }

    public Book getBook(long id){
        return bookRepository.findById(id).get();
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
        Book entityBook = BookRequestDTO.convertToEntity(bookRequestDTO);
        bookRepository.save(entityBook);
        numberOfBooksAdded.increment();
    }

    public void updateUserForBook(long bookId, long userId) {
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        if (bookOptional.isPresent()) {
            Book book = bookOptional.get();
            bookRepository.save(book);
        } else {
            timesBookNotFound.increment();
            throw new NoSuchElementException("No such book is present with id" + bookId);
        }
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
