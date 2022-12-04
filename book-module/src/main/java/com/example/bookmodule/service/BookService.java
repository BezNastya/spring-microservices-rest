package com.example.bookmodule.service;

import com.example.bookmodule.dto.BookDTO;
import com.example.bookmodule.dto.BookRequestDTO;
import com.example.bookmodule.dto.BooksList;
import com.example.bookmodule.entity.Book;
import com.example.bookmodule.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.bookmodule.config.ActiveMQConfiguration.*;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;
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
