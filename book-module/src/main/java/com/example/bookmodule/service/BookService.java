package com.example.bookmodule.service;

import com.example.bookmodule.dto.BookDTO;
import com.example.bookmodule.dto.BookRequestDTO;
import com.example.bookmodule.dto.BooksList;
import com.example.bookmodule.entity.Book;
import com.example.bookmodule.model.BookProto;
import com.example.bookmodule.repository.BookRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
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

    public BookProto.Book getBook(long id){
        Book found =  bookRepository.findById(id).get();
        BookProto.Book test = BookProto.Book.newBuilder()
                .setId(found.getId())
                .setAuthorId(found.getAuthorId())
                .setName(found.getName())
                .build();
        return test;
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
    public void deleteBookWithAuthor(BookProto.Book b){
        Book input = new Book();
        input.setId(b.getId());
        input.setAuthorId(b.getAuthorId());
        input.setName(b.getName());
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
