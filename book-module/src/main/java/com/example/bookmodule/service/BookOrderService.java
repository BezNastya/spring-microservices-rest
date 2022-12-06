package com.example.bookmodule.service;

import com.example.bookmodule.dto.BookOrderDTO;
import com.example.bookmodule.dto.BookOrderList;
import com.example.bookmodule.feign.OrderModuleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.example.bookmodule.config.ActiveMQConfiguration.*;

@Service
public class BookOrderService {
    @Autowired
    private OrderModuleClient orderModuleClient;
    @Autowired
    private JmsTemplate jmsTemplate;

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
