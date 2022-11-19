package com.example.bookmodule.service;

import com.example.bookmodule.dto.BookOrderDTO;
import com.example.bookmodule.dto.BookOrderList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.example.bookmodule.config.ActiveMQConfiguration.*;

@Service
public class BookOrderService {
    private RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private JmsTemplate jmsTemplate;

    public List<Long> getAllBooksInOrders() {
        BookOrderList bookOrderList = restTemplate
                .getForObject("http://localhost:8004/orders", BookOrderList.class);
        return bookOrderList
                .getBookOrders()
                .stream()
                .map(BookOrderDTO::getBookId)
                .collect(Collectors.toList());
    }

    public List<Long> getAllBooksInOrdersByUserId(long userId) {
        BookOrderList bookOrderList = restTemplate
                .getForObject("http://localhost:8004/orders/" + userId, BookOrderList.class);
        return bookOrderList
                .getBookOrders()
                .stream()
                .map(BookOrderDTO::getBookId)
                .collect(Collectors.toList());
    }

    public void createOrder(long bookId, long userId) {
        if (getAllBooksInOrders().contains(bookId)) {
            throw new RuntimeException("Order for book already exists");
        }
        BookOrderDTO bookOrderDTO = new BookOrderDTO();
        bookOrderDTO.setBookId(bookId);
        bookOrderDTO.setUserId(userId);
        jmsTemplate.convertAndSend(ORDER_QUEUE, bookOrderDTO, message -> {
            message.setJMSType(NEW_ORDER_JMS_TYPE);
            return message;
        });
    }

    public void cancelOrder(long bookId, long userId) {
        if (!getAllBooksInOrdersByUserId(userId).contains(bookId)) {
            throw new NoSuchElementException("No order for book for current user");
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
