package com.example.ordermodule.service;

import com.example.ordermodule.dto.BookOrderDTO;
import com.example.ordermodule.dto.BookOrderList;
import com.example.ordermodule.entity.BookOrder;
import com.example.ordermodule.repository.BookOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.ordermodule.config.ActiveMQConfiguration.*;

@Service
@Slf4j
public class BookOrderService {


    @Autowired
    private BookOrderRepository bookOrderRepository;

    public BookOrderList getAllBookOrders() {
        List<BookOrder> bookOrders = bookOrderRepository.findAll();
        List<BookOrderDTO> bookOrderDTOS = bookOrders.stream()
                .map(BookOrderDTO::new)
                .collect(Collectors.toList());
        return new BookOrderList(bookOrderDTOS);
    }

    public BookOrderList getAllBookOrdersByUserId(long userId) {
        List<BookOrder> bookList = bookOrderRepository.findAll();
        List<BookOrderDTO> bookOrderDTOS = bookList.stream()
                .filter(x -> x.getUserId() == userId)
                .map(BookOrderDTO::new)
                .collect(Collectors.toList());
        return new BookOrderList(bookOrderDTOS);
    }

    @JmsListener(destination = ORDER_QUEUE, selector = "JMSType = 'NEW'")
    public void addBookOrder(BookOrderDTO bookOrderDTO) {
        BookOrder bookOrder = BookOrderDTO.convertToEntity(bookOrderDTO);
        bookOrderRepository.save(bookOrder);
        log.info("Successfully created new book order");
    }

    @JmsListener(destination = ORDER_QUEUE, selector = "JMSType = 'CANCEL'")
    public void cancelBookOrder(BookOrderDTO bookOrderDTO) {
        BookOrder bookOrder = bookOrderRepository.findBookOrderByBookIdAndUserId(bookOrderDTO.getBookId(), bookOrderDTO.getUserId());
        if (Objects.isNull(bookOrder)) {
            log.info("Order already canceled");
            return;
        }
        bookOrderRepository.delete(bookOrder);
        log.info("Successfully canceled book order");
    }

}
