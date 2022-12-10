package com.example.usermodule;

import com.example.usermodule.feign.BookModuleClient;
import com.example.usermodule.repositories.RoleRepository;
import com.example.usermodule.repositories.UserRepository;
import com.example.usermodule.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@AutoConfigureMockMvc(addFilters = false)
@Import(Properties.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserControllerTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository mockUserRepository;

    @Autowired
    private RoleRepository mockRoleRepository;


    @MockBean
    private BookModuleClient bookModuleClient;

    private final List<User> mockUsers = new ArrayList<>();

    // Book list for User with id 1
    private final BooksList mockBookListForUser = new BooksList();

    {
        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");

        User user = new User();
        user.setLogin("user");
        user.setPassword("$2a$04$cmvr8QTVpTxrz2XW3loxWORcgy5t0SzR4gQI.WrRnGEQaKVapAjW6");
        user.setAge(5);
        user.setId(3L);
        user.setRoles(Collections.singletonList(userRole));

        User admin = new User();
        admin.setLogin("admin");
        admin.setPassword("$2a$04$0swp2JawQzpHDC90bxFog.5s8HmglaWIVLzevnJX9z1fsk6mcvxzK");
        admin.setAge(5);
        admin.setId(1L);
        user.setRoles(Collections.singletonList(adminRole));

        mockUsers.add(user);
        mockUsers.add(admin);

        mockBookListForUser.setBooks(new ArrayList<>());
        mockBookListForUser.getBooks().add(new Book(5, "b1"));
        mockBookListForUser.getBooks().add(new Book(8, "b2"));
        mockBookListForUser.getBooks().add(new Book(10, "b3"));
    }

    @BeforeEach
    public void setUpMocks() {
        MockitoAnnotations.openMocks(this);

        when(bookModuleClient.getBooksByAuthor(any(), any()))
                .thenReturn(mockBookListForUser);
    }


    @Test
    public void test_getAllUsers() {
        final Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        final List<Role> roleAdmin = List.of(adminRole);


        webTestClient
                .get().uri("/users/all")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .consumeWith(b -> {
                    final List<User> usersList = b.getResponseBody();
                    System.out.println(usersList.toString());

                    assertNotNull(usersList);
                    assertEquals(mockUsers.size(), usersList.size());
                });
    }

    @Test
    public void test_get_id() {
        final Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        final List<Role> roleAdmin = List.of(adminRole);


        webTestClient
                .get().uri("/users/3")
                .headers(http -> http.add("Authorization", "Bearer_dummy"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .consumeWith(b -> {
                    UserDto response = b.getResponseBody();
                    assertNotNull(response);
                    assertEquals(mockUsers.get(0).getId(), response.getId());
                    assertEquals(mockUsers.get(0).getLogin(), response.getLogin());
                    final List<Book> bookList = response.getBookList();
                    assertNotNull(bookList);
                    assertEquals(mockBookListForUser.getBooks().size(), bookList.size());
                    mockBookListForUser.getBooks().forEach(book -> assertTrue(bookList.contains(book)));
                });
    }

    @Test
    public void test_get_file() throws Exception {
        final Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        final List<Role> roleAdmin = List.of(adminRole);


        webTestClient
                .get().uri("/users/file/3")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM);
    }
}
