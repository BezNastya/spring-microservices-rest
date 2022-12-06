package com.example.ordermodule;


import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/management")
public class UserManagmentController {
    @Autowired
    private JmsTemplate jmsTemplate;


    @PostMapping("/{userId}")
    public void getAllBookOrdersByUser(@PathVariable long userId) throws URISyntaxException {
        jmsTemplate.convertAndSend("user_management", userId);
    }
}
