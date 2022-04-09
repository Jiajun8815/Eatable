package com.example.eatable.service.listener;

import com.example.eatable.configuration.MessageConfiguration;
import com.example.eatable.infrastructure.database.entity.InvoiceConfirmationEntity;
import com.example.eatable.infrastructure.database.repository.InvoiceConfirmationRepository;
import com.example.eatable.infrastructure.database.repository.InvoiceRequestRepository;
import com.example.eatable.infrastructure.mq.model.InvoiceMessage;
import com.example.eatable.infrastructure.mq.model.mapper.InvoiceMessageMapper;
import com.example.eatable.infrastructure.restful.RestClient;
import com.example.eatable.infrastructure.restful.model.InvoiceResponse;
import com.example.eatable.infrastructure.restful.model.InvoiceResultResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class InvoiceListener {
    public static final long MAX_MESSAGE_LIFE_CYCLE = 2L;

    private final RestClient restClient;

    private final RabbitTemplate rabbitTemplate;

    private final InvoiceConfirmationRepository invoiceConfirmationRepository;

    @Value("${http.url.invoice}")
    private String invoiceEndpoint;

    public InvoiceListener(RestClient restClient, RabbitTemplate rabbitTemplate, InvoiceRequestRepository invoiceRequestRepository, InvoiceConfirmationRepository invoiceConfirmationRepository) {
        this.restClient = restClient;
        this.rabbitTemplate = rabbitTemplate;
        this.invoiceConfirmationRepository = invoiceConfirmationRepository;
    }


    @RabbitListener(queues = {MessageConfiguration.REQUEST_QUEUE_NAME, MessageConfiguration.RETRY_QUEUE_NAME})
    public void handleRequestMessage(InvoiceMessage message) {
        LocalDateTime now = LocalDateTime.now();
        if (isInvalid(message, now)) {
            invoiceConfirmationRepository.save(InvoiceConfirmationEntity.builder()
                    .result("failed")
                    .createAt(now)
                    .contactId(message.getContactId()).build());
            return;
        }

        try {
            ResponseEntity<InvoiceResponse> response = restClient.post(invoiceEndpoint, InvoiceMessageMapper.MAPPER.messageToRequest(message), InvoiceResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                rabbitTemplate.convertAndSend(MessageConfiguration.TO_CONFIRM_QUEUE_NAME, message);
            } else {
                rabbitTemplate.convertAndSend(MessageConfiguration.RETRY_QUEUE_NAME, message);
            }
        } catch (RestClientException e) {
            rabbitTemplate.convertAndSend(MessageConfiguration.RETRY_QUEUE_NAME, message);
        }
    }

    @RabbitListener(queues = MessageConfiguration.TO_CONFIRM_QUEUE_NAME)
    public void handleConfirmationMessage(InvoiceMessage message) {
        LocalDateTime now = LocalDateTime.now();
        if (isInvalid(message, now)) {
            invoiceConfirmationRepository.save(InvoiceConfirmationEntity.builder()
                    .result("failed")
                    .createAt(now)
                    .contactId(message.getContactId()).build());
            return;
        }

        Optional<InvoiceConfirmationEntity> confirmation = invoiceConfirmationRepository.findByContactId(message.getContactId());
        if (confirmation.isPresent()) {
            return;
        }

        ResponseEntity<InvoiceResultResponse> invoiceResultResponse = restClient.get(invoiceEndpoint, InvoiceResultResponse.class);
        if (invoiceResultResponse.getStatusCode().is2xxSuccessful()) {
            invoiceConfirmationRepository.save(InvoiceConfirmationEntity.builder()
                    .result(invoiceResultResponse.getBody().getResult())
                    .createAt(now)
                    .contactId(message.getContactId()).build());
            return;
        }

        rabbitTemplate.convertAndSend(MessageConfiguration.TO_CONFIRM_QUEUE_NAME, message);
    }

    private boolean isInvalid(InvoiceMessage message, LocalDateTime localDateTime) {
        long duration = Duration.between(message.getCreateAt(), localDateTime).toDays();
        return duration >= MAX_MESSAGE_LIFE_CYCLE;
    }
}
