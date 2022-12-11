package com.example.bookmodule;

import com.example.bookmodule.config.jwt.JwtTokenProvider;
import com.example.bookmodule.dto.*;
import com.example.bookmodule.entity.Book;
import com.example.bookmodule.feign.OrderModuleClient;
import com.example.bookmodule.repository.BookRepository;
import com.example.bookmodule.service.BookOrderService;
import com.example.bookmodule.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static com.example.bookmodule.config.ActiveMQConfiguration.ORDER_QUEUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookAPIIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private BookService bookService;
    @Autowired
    private BookRepository bookRepository;

    private BookOrderService bookOrderService;

    @MockBean
    private OrderModuleClient orderModuleClient;
    @MockBean
    private JmsTemplate jmsTemplate;


    private BooksList booksAll;
    private BooksList booksAuthor;
    private final BookRequestDTO addBookDto = new BookRequestDTO("name", 1, "first", "asdf");

    private String TEST_TOKEN;
    private long EXISTING_BOOK_ID;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void initAll() {
        this.bookOrderService = new BookOrderService(orderModuleClient, jmsTemplate, bookService);
        bookService.deleteAll();
        Book testBook = new Book();
        testBook.setName("name");
        testBook.setAuthorId(1);
        EXISTING_BOOK_ID = bookRepository.save(testBook).getId();
        booksAll = bookService.getAllBooks();
        booksAuthor = bookService.getAllBooksByAuthorId(1);
        TEST_TOKEN = jwtTokenProvider.createToken("test");
    }

    @Test
    public void whenGetAllBooks_BookServiceWorksFine_receiveOk() throws Exception {
        MvcResult res = mvc.perform(get("/books/all")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer_" + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BooksList actualBookList = objectMapper.readValue(res.getResponse().getContentAsString(), BooksList.class);
        Assertions.assertEquals(booksAll, actualBookList);
    }

    @Test
    public void whenGetBooksByAuthor_BookServiceWorksFine_receiveOk() throws Exception {
        int authorId = 1;
        MvcResult res = mvc.perform(get("/books/booksByAuthor/" + authorId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer_" + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BooksList actualBookList = objectMapper.readValue(res.getResponse().getContentAsString(), BooksList.class);

        Assertions.assertNotNull(actualBookList);
        Assertions.assertEquals(booksAuthor, actualBookList);
    }

    @Test
    public void whenAddBook_BookServiceWorksFine_receiveOk_NoBody() throws Exception {
        BookRequestDTO addBookDto = new BookRequestDTO("another", 1, "first", "asdf");

        mvc.perform(post("/books/add")
                .content(objectMapper.writeValueAsString(addBookDto))
                .header(HttpHeaders.AUTHORIZATION, "Bearer_" + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void whenAddBook_bookAlreadyExists_receiveBadRequest() throws Exception{
        mvc.perform(post("/books/add")
                        .content(objectMapper.writeValueAsString(addBookDto))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer_" + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenAddOrder_OrderServiceWorksFine_receiveOk_NoBody() throws Exception {
        BookOrderList orderList = new BookOrderList();;
        orderList.setBookOrders(List.of());
        when(orderModuleClient.getAllBookOrders()).thenReturn(orderList);

        mvc.perform(put("/books/order/" + EXISTING_BOOK_ID + "/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer_" + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(jmsTemplate).convertAndSend(eq(ORDER_QUEUE), any(), any());
        verify(orderModuleClient).getAllBookOrders();
    }

    @Test
    public void whenAddOrder_BookDoesNotExists_receiveNotFound() throws Exception {

        mvc.perform(put("/books/order/1/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer_" + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(jmsTemplate, never()).convertAndSend(eq(ORDER_QUEUE), any(), any());
        verify(orderModuleClient, never()).getAllBookOrders();
    }

    @Test
    public void whenAddOrder_OrderExists_receiveBadRequest() throws Exception {
        BookOrderList orderList = new BookOrderList();
        BookOrderDTO bookOrderDTO = new BookOrderDTO();
        bookOrderDTO.setBookId(EXISTING_BOOK_ID);
        orderList.setBookOrders(List.of(bookOrderDTO));
        when(orderModuleClient.getAllBookOrders()).thenReturn(orderList);

        mvc.perform(put("/books/order/" + EXISTING_BOOK_ID + "/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer_" + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderModuleClient).getAllBookOrders();
        verify(jmsTemplate, never()).convertAndSend(eq(ORDER_QUEUE), any(), any());
    }

    @Test
    public void whenCancelOrder_OrderServiceWorksFine_receiveOk_NoBody() throws Exception {
        BookOrderList orderList = new BookOrderList();
        BookOrderDTO bookOrderDTO = new BookOrderDTO();
        bookOrderDTO.setBookId(EXISTING_BOOK_ID);
        orderList.setBookOrders(List.of(bookOrderDTO));
        when(orderModuleClient.getBookOrdersByUserId(1L)).thenReturn(orderList);
        mvc.perform(put("/books/cancel/" + EXISTING_BOOK_ID + "/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer_" + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(jmsTemplate).convertAndSend(eq(ORDER_QUEUE), any(), any());
        verify(orderModuleClient).getBookOrdersByUserId(1L);
    }

    @Test
    public void whenCancelOrder_OrderDoesNotExists_receiveNotFound() throws Exception {
        BookOrderList orderList = new BookOrderList();
        orderList.setBookOrders(List.of());
        when(orderModuleClient.getBookOrdersByUserId(1L)).thenReturn(orderList);
        mvc.perform(put("/books/cancel/" + EXISTING_BOOK_ID + "/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer_" + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(orderModuleClient).getBookOrdersByUserId(1L);
        verify(jmsTemplate, never()).convertAndSend(eq(ORDER_QUEUE), any(), any());
    }

    @Test
    public void whenDeleteBook_BookExists_receiveOk() throws Exception {
        MvcResult res = mvc.perform(delete("/books/delete/" + EXISTING_BOOK_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer_" + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals("Deleted " + EXISTING_BOOK_ID, res.getResponse().getContentAsString());
    }

    @Test
    public void whenDeleteBook_BookDoesNotExists_receiveNotFound() throws Exception {
        mvc.perform(delete("/books/delete/11111")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer_" + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
