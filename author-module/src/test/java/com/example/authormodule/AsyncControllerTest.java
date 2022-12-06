package com.example.authormodule;

import com.example.authormodule.dto.AuthorDto;
import com.example.authormodule.dto.Book;
import com.example.authormodule.dto.BooksList;
import com.example.authormodule.entities.Author;
import com.example.authormodule.entities.Role;
import com.example.authormodule.feign.BookModuleClient;
import com.example.authormodule.services.AuthorRepository;
import jwt.JwtTokenProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@AutoConfigureMockMvc
@Import(Properties.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AsyncControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthorRepository mockAuthorRepository;

    @MockBean
    private BookModuleClient bookModuleClient;

    private final List<Author> mockAuthors = new ArrayList<>();

    // Book list for Author with id 1
    private final BooksList mockBookListForAuthor = new BooksList();

    {
        mockAuthors.add(new Author(1L, "FN1", "LN1"));
        mockAuthors.add(new Author(2L, "FN2", "LN2"));
        mockAuthors.add(new Author(3L, "FN2", "LN3"));

        mockBookListForAuthor.setBooks(new ArrayList<>());
        mockBookListForAuthor.getBooks().add(new Book(5, "b1"));
        mockBookListForAuthor.getBooks().add(new Book(8, "b2"));
        mockBookListForAuthor.getBooks().add(new Book(10, "b3"));
    }


    @BeforeEach
    public void setUpMocks() {
        MockitoAnnotations.openMocks(this);
        doReturn(mockAuthors).when(mockAuthorRepository).findAll();
        doReturn(Optional.of(mockAuthors.get(0))).when(mockAuthorRepository).findById(1L);
        doReturn(Optional.of(mockAuthors.get(1))).when(mockAuthorRepository).findById(2L);
        doReturn(Optional.of(mockAuthors.get(2))).when(mockAuthorRepository).findById(3L);

        when(bookModuleClient.getBooksByAuthor(any(), any())).thenReturn(mockBookListForAuthor);

    }

    @Test
    public void shouldNotAllowAccessToUnauthenticatedUsers() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/all")).andExpect(status().isForbidden());
        mvc.perform(MockMvcRequestBuilders.get("/withBooks/1")).andExpect(status().isForbidden());
        mvc.perform(MockMvcRequestBuilders.get("/1")).andExpect(status().isForbidden());
    }

    @Test
    public void test_withValidToken_receiveOk() {
        final Role adminRole = new Role();
        adminRole.setName("ADMIN");
        final List<Role> roleAdmin = List.of(adminRole);

        String tokenString = jwtTokenProvider.createToken("admin", roleAdmin);

        webTestClient
                .get().uri("/all")
                .headers(http -> http.add("Authorization", "Bearer_" + tokenString))
                .exchange()
                .expectStatus().isOk()
                .expectBody().consumeWith(Assertions::assertNotNull);
    }

    @Test
    public void test_getAllAuthors() {
        final Role adminRole = new Role();
        adminRole.setName("ADMIN");
        final List<Role> roleAdmin = List.of(adminRole);

        String tokenString = jwtTokenProvider.createToken("admin", roleAdmin);

        webTestClient
                .get().uri("/all")
                .headers(http -> http.add("Authorization", "Bearer_" + tokenString))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Author.class)
                .consumeWith(b -> {
                    final List<Author> authorList = b.getResponseBody();
                    assertNotNull(authorList);
                    assertEquals(mockAuthors.size(), authorList.size());
                    mockAuthors.forEach(author -> assertTrue(authorList.contains(author)));
                });
    }

    @Test
    public void test_get_id() {
        final Role adminRole = new Role();
        adminRole.setName("ADMIN");
        final List<Role> roleAdmin = List.of(adminRole);

        String tokenString = jwtTokenProvider.createToken("admin", roleAdmin);

        webTestClient
                .get().uri("/1")
                .headers(http -> http.add("Authorization", "Bearer_" + tokenString))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Author.class)
                .consumeWith(b -> {
                    final Author responseAuthor = b.getResponseBody();
                    assertNotNull(responseAuthor);
                    assertEquals(mockAuthors.get(0), responseAuthor);
                });
    }

    @Test
    public void test_get_withBooks() {
        final Role adminRole = new Role();
        adminRole.setName("ADMIN");
        final List<Role> roleAdmin = List.of(adminRole);

        String tokenString = jwtTokenProvider.createToken("admin", roleAdmin);

        webTestClient
                .get().uri("/withBooks/1")
                .headers(http -> http.add("Authorization", "Bearer_" + tokenString))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthorDto.class)
                .consumeWith(b -> {
                    AuthorDto response = b.getResponseBody();
                    assertEquals(mockAuthors.get(0).getId(), response.getId());
                    assertEquals(mockAuthors.get(0).getFirstname(), response.getFirstname());
                    assertEquals(mockAuthors.get(0).getLastname(), response.getLastname());
                    final List<Book> bookList = response.getBookList();
                    assertNotNull(bookList);
                    assertEquals(mockBookListForAuthor.getBooks().size(), bookList.size());
                    mockBookListForAuthor.getBooks().forEach(book -> assertTrue(bookList.contains(book)));
                });
    }


}
