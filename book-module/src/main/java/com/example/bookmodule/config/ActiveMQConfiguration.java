package com.example.bookmodule.config;

import com.example.bookmodule.dto.BookOrderDTO;
import com.example.bookmodule.dto.BookRequestDTO;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableJms
@Configuration
public class ActiveMQConfiguration {

    public static final String ORDER_QUEUE = "book_orders";
    public static final String AUTHOR_QUEUE = "author_queue";
    public static final String USERS_BOOK_QUEUE = "USERS_BOOK_QUEUE";
    public static final String BOOK_QUEUE = "new_book";
    public static final String BOOK_WITH_AUTHOR_QUEUE = "book_author";

    public static final String NEW_ORDER_JMS_TYPE = "NEW";
    public static final String CANCEL_ORDER_JMS_TYPE= "CANCEL";
    public static final String DELETE_ORDER_JMS_TYPE= "DELETE";


    @Bean
    public JmsListenerContainerFactory<?> queueListenerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    @Bean(name = "converter")
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("JMS_TYPE", BookOrderDTO.class);
        typeIdMappings.put("Test", BookRequestDTO.class);
        converter.setTypeIdMappings(typeIdMappings);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

}
