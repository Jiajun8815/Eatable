package com.example.eatable.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConfiguration {

    public final static String REQUEST_QUEUE_NAME = "amqp.request.queue";
    public final static String RETRY_QUEUE_NAME = "amqp.retry.queue";
    public final static String TO_CONFIRM_QUEUE_NAME = "amqp.confirmation.queue";

    @Bean
    public Queue requestQueue() {
        return new Queue(REQUEST_QUEUE_NAME, false);
    }

    @Bean
    public Queue retryQueue() {
        return new Queue(RETRY_QUEUE_NAME, false);
    }

    @Bean
    public Queue confirmationQueue() {
        return new Queue(TO_CONFIRM_QUEUE_NAME, false);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        objectMapper.registerModules(javaTimeModule);
        return objectMapper;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory, final ObjectMapper objectMapper) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter(objectMapper));
        return rabbitTemplate;
    }

}
