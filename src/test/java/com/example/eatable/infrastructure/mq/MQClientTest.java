package com.example.eatable.infrastructure.mq;

import com.example.eatable.configuration.MessageConfiguration;
import com.example.eatable.infrastructure.mq.model.InvoiceMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@ContextConfiguration(initializers = MQClientTest.Initializer.class)
class MQClientTest {
    @ClassRule
    private static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq"));

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    @BeforeAll
    public static void setUpAll() {
        rabbitMQContainer.start();
    }

    @AfterAll
    public static void tearDown() {
        rabbitMQContainer.stop();
    }

    @BeforeEach
    void setUp() {
        rabbitListenerEndpointRegistry.getListenerContainers()
                .forEach(Lifecycle::stop);
    }

    @Test
    @SneakyThrows
    public void should_send_and_receive_message_successful_when_queue_exist() {
        InvoiceMessage message = InvoiceMessage.builder()
                .contactId(1)
                .amount("2000")
                .createAt(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(MessageConfiguration.REQUEST_QUEUE_NAME, message);
        await().atMost(40, TimeUnit.SECONDS);
        Message receive = rabbitTemplate.receive(MessageConfiguration.REQUEST_QUEUE_NAME);

        assertEquals(objectMapper.writeValueAsString(message), new String(receive.getBody()));

    }

    public static class Initializer implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues testPropertyValues = TestPropertyValues.of(
                    "spring.rabbitmq.host=" + rabbitMQContainer.getContainerIpAddress(),
                    "spring.rabbitmq.port=" + rabbitMQContainer.getMappedPort(5672),
                    "spring.rabbitmq.username=" + rabbitMQContainer.getAdminUsername(),
                    "spring.rabbitmq.password=" + rabbitMQContainer.getAdminPassword()
            );
            testPropertyValues.applyTo(configurableApplicationContext);
        }
    }
}
