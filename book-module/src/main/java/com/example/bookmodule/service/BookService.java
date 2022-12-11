package com.example.bookmodule.service;

import com.example.book.module.AuthorRequest;
import com.example.book.module.AuthorResponse;
import com.example.book.module.AuthorSendServiceGrpc;
import com.example.book.module.BookProto;
import com.example.bookmodule.dto.BookDTO;
import com.example.bookmodule.dto.BookFullDTO;
import com.example.bookmodule.dto.BookRequestDTO;
import com.example.bookmodule.dto.BooksList;
import com.example.bookmodule.entity.Book;
import com.example.bookmodule.repository.BookRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.bookmodule.config.ActiveMQConfiguration.*;

@Service
@Slf4j
public class BookService {


    @Autowired
    private BookOrderService bookOrderService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @GrpcClient("grpc-author-book-service")
    AuthorSendServiceGrpc.AuthorSendServiceBlockingStub client;

    private final BookRepository bookRepository;
    private final MeterRegistry meterRegistry;
    private final Counter numberOfBooksAdded;
    private final Counter timesBookNotFound;

    @Autowired
    public BookService(BookRepository bookRepository, MeterRegistry meterRegistry) {
        this.bookRepository = bookRepository;
        this.meterRegistry = meterRegistry;
        this.numberOfBooksAdded = this.meterRegistry.counter("books.actions", "action", "add");
        this.timesBookNotFound = this.meterRegistry.counter("books.exception", "type", "not-found");
    }


    public BooksList getAllBooks() {
        List<Book> bookList = bookRepository.findAll();
        List<BookDTO> bookDTOS = bookList.stream()
                .map(BookDTO::new)
                .collect(Collectors.toList());
        return new BooksList(bookDTOS);
    }

    public BookProto.Book getProtoBook(long id){
        Book found =  bookRepository.findById(id).get();
        BookProto.Book test = BookProto.Book.newBuilder()
                .setId(found.getId())
                .setAuthorId(found.getAuthorId())
                .setName(found.getName())
                .build();
        return test;
    }

    public Book getBook(long id) {
        return bookRepository.findById(id).orElse(null);
    }

    public BookFullDTO getBookWithAuthor(long id) {
        Optional<Book> b = bookRepository.findById(id);
        if (b.isPresent()) {
            Book book = b.get();
            AuthorResponse authorResponse = client.getAuthor(AuthorRequest.newBuilder().setId(book.getId()).build());
            return BookFullDTO.builder()
                    .id(book.getId())
                    .name(book.getName())
                    .author(BookFullDTO.AuthorDTO.builder()
                            .id(authorResponse.getId())
                            .firstname(authorResponse.getFirstname())
                            .lastname(authorResponse.getLastname())
                            .build())
                    .build();
        } else throw new NoSuchElementException();
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
            throw new RuntimeException("Book is already exists.");
        }

        jmsTemplate.convertAndSend(BOOK_QUEUE, bookRequestDTO);
        numberOfBooksAdded.increment();
    }

    @JmsListener(destination = BOOK_WITH_AUTHOR_QUEUE)
    public void addBookWithAuthor(BookRequestDTO bookRequestDTO) {
        Book entityBook = BookRequestDTO.convertToEntity(bookRequestDTO);
        bookRepository.save(entityBook);
        log.info("New book added: " + entityBook);
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

    @Transactional
    public void deleteBookWithAuthor(Book b) {
        Book input = new Book();
        input.setId(b.getId());
        input.setAuthorId(b.getAuthorId());
        input.setName(b.getName());
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

}
