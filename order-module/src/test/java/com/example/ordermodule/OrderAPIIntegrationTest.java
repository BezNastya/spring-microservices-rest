package com.example.ordermodule;

import com.example.ordermodule.dto.BookOrderList;
import com.example.ordermodule.entity.BookOrder;
import com.example.ordermodule.repository.BookOrderRepository;
import com.example.ordermodule.service.BookOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderAPIIntegrationTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private BookOrderRepository orderRepository;
    @Autowired
    private BookOrderService bookOrderService;


    private BookOrderList ordersAll;
    private BookOrderList ordersUser;

    private ObjectMapper objectMapper = new ObjectMapper();

    private long USER_ID = 1;

    @BeforeEach
    public void initAll() {
        orderRepository.deleteAll();
        BookOrder testOrder = new BookOrder();
        testOrder.setUserId(USER_ID);
        testOrder.setBookId(1);
        ordersAll = bookOrderService.getAllBookOrders();
        ordersUser = bookOrderService.getAllBookOrdersByUserId(USER_ID);
    }

    @Test
    public void whenGetAllOrders_receiveOk() throws Exception {
        MvcResult res = mvc.perform(get("/orders/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookOrderList actualOrderList = objectMapper.readValue(res.getResponse().getContentAsString(), BookOrderList.class);

        Assertions.assertNotNull(actualOrderList);
        Assertions.assertEquals(ordersAll, actualOrderList);
    }

    @Test
    public void whenGetOrdersByUser_receiveOk() throws Exception {
        MvcResult res = mvc.perform(get("/orders/" + USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookOrderList bookOrderList = objectMapper.readValue(res.getResponse().getContentAsString(), BookOrderList.class);

        Assertions.assertNotNull(bookOrderList);
        Assertions.assertEquals(ordersUser, bookOrderList);
    }

}
