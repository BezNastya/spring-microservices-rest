package com.example.frontendmodule;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnMainPage() throws Exception {
        this.mockMvc.perform(get("/home")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome to library!")))
                .andExpect(content().string(containsString("A reader lives thousand lives before he dies")))
                .andExpect(content().string(containsString("The frontend is currently under development, please use API")));
    }
}
