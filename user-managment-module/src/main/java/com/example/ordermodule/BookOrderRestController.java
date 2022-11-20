package com.example.ordermodule;


import java.net.URI;
import java.net.URISyntaxException;

import com.example.ordermodule.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
public class BookOrderRestController {
    @Autowired
    private JmsTemplate jmsTemplate;


    @DeleteMapping("/management/{userId}")
    public void getAllBookOrdersByUser(@PathVariable long userId) throws URISyntaxException {
//        URI uri = new URI("http://localhost:8001/" + userId);
//       restTemplate.delete(uri);
        jmsTemplate.convertAndSend("user_management", userId);


    }
}
