package com.example.adminmodule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
public class AdminController {
    private Log log = LogFactory.getLog(AdminController.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @GetMapping("/change/{level}")
    public void send(@PathVariable String level) {
        log.info("sending level to set up ='"+level+"'");
        jmsTemplate.convertAndSend("EmpTopic", level);
    }

    @GetMapping
    public String log() {
        log.trace("This is a TRACE level message");
        log.debug("This is a DEBUG level message");
        log.info("This is an INFO level message");
        log.warn("This is a WARN level message");
        log.error("This is an ERROR level message");
        return "See the log for details";
    }
}
