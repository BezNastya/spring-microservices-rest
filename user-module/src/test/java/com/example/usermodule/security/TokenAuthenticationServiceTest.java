package com.example.usermodule.security;

import com.example.usermodule.Role;
import com.example.usermodule.repositories.RoleRepository;
import com.example.usermodule.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Properties;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@AutoConfigureMockMvc
@Import(Properties.class)
public class TokenAuthenticationServiceTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    WebTestClient webTestClient;

    @Test
    public void shouldNotAllowAccessToUnauthenticatedUsers() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/all")).andExpect(status().isForbidden());
    }

    @Test
    public void test_withValidToken_receiveOk() {
        final Role adminRole = new Role();
        adminRole.setName("ADMIN");
        final List<Role> roleAdmin = List.of(adminRole);

        var tokenString = jwtTokenProvider.createToken("admin", roleAdmin);

        webTestClient
                .get().uri("/all")
                .headers(http -> http.add("Authorization", "Bearer_" + tokenString))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void test_withInvalidToken_receive401() {
        final Role userRole = new Role();
        userRole.setName("USER");
        final List<Role> roleUser = List.of(userRole);

        var tokenString = jwtTokenProvider.createToken("user", roleUser);

        webTestClient
                .get().uri("/all")
                .headers(http -> http.add("Authorization", "Bearer_" + tokenString))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void test_withoutAuthToken_receive401() {

        webTestClient
                .get().uri("/all")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void testsave_withValidToken_receiveNotFound_forAdmin() {
        final Role adminRole = new Role();
        adminRole.setName("ADMIN");
        final List<Role> roleAdmin = List.of(adminRole);

        var tokenString = jwtTokenProvider.createToken("admin", roleAdmin);

        webTestClient
                .get().uri("/1/save")
                .headers(http -> http.add("Authorization", "Bearer_" + tokenString))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testsave_withValidToken_receiveNotFound_forUser() {
        final Role userRole = new Role();
        userRole.setName("USER");
        final List<Role> roleUser = List.of(userRole);

        var tokenString = jwtTokenProvider.createToken("user", roleUser);

        webTestClient
                .get().uri("/1/save")
                .headers(http -> http.add("Authorization", "Bearer_" + tokenString))
                .exchange()
                .expectStatus().isNotFound();
    }


}