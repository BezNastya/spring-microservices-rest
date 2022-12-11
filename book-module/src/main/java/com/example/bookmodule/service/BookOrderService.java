package com.example.bookmodule.service;

import com.example.bookmodule.dto.BookOrderDTO;
import com.example.bookmodule.dto.BookOrderList;
import com.example.bookmodule.exception.NoSuchBookException;
import com.example.bookmodule.exception.NoSuchOrderException;
import com.example.bookmodule.exception.OrderAlreadyExistsException;
import com.example.bookmodule.feign.OrderModuleClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.example.bookmodule.config.ActiveMQConfiguration.*;

@Service
@Slf4j
public class BookOrderService {
    private OrderModuleClient orderModuleClient;
    private JmsTemplate jmsTemplate;
    private BookService bookService;

    @Autowired
    public BookOrderService(OrderModuleClient orderModuleClient,
                            JmsTemplate jmsTemplate, BookService bookService) {
        this.orderModuleClient = orderModuleClient;
        this.jmsTemplate = jmsTemplate;
        this.bookService = bookService;
    }

    public List<Long> getAllBooksInOrders() {
        BookOrderList bookOrderList = orderModuleClient.getAllBookOrders();
        return bookOrderList
                .getBookOrders()
                .stream()
                .map(BookOrderDTO::getBookId)
                .collect(Collectors.toList());
    }

    public List<Long> getAllBooksInOrdersByUserId(long userId) {
        BookOrderList bookOrderList = orderModuleClient.getBookOrdersByUserId(userId);
        return bookOrderList
                .getBookOrders()
                .stream()
                .map(BookOrderDTO::getBookId)
                .collect(Collectors.toList());
    }

    public void createOrder(long bookId, long userId) {
        //Call it to check that book exists
        bookService.getBook(bookId);
        if (getAllBooksInOrders().contains(bookId)) {
            throw new OrderAlreadyExistsException("Order for book already exists");
        }
        BookOrderDTO bookOrderDTO = new BookOrderDTO();
        bookOrderDTO.setBookId(bookId);
        bookOrderDTO.setUserId(userId);
        jmsTemplate.convertAndSend(ORDER_QUEUE, bookOrderDTO, message -> {
            message.setJMSType(NEW_ORDER_JMS_TYPE);
            log.info("Sending message to order queue");
            return message;
        });
        log.info("Sent message to order queue");
    }

    public void cancelOrder(long bookId, long userId) {
        //Call it to check that book exists
        bookService.getBook(bookId);
        if (!getAllBooksInOrdersByUserId(userId).contains(bookId)) {
            throw new NoSuchOrderException("No order for book for current user");
        }
        BookOrderDTO bookOrderDTO = new BookOrderDTO();
        bookOrderDTO.setBookId(bookId);
        bookOrderDTO.setUserId(userId);
        jmsTemplate.convertAndSend(ORDER_QUEUE, bookOrderDTO, message -> {
            message.setJMSType(CANCEL_ORDER_JMS_TYPE);
            return message;
        });
    }
}
