package com.example.eatable.service.listener;

import com.example.eatable.configuration.MessageConfiguration;
import com.example.eatable.infrastructure.database.entity.InvoiceConfirmationEntity;
import com.example.eatable.infrastructure.database.repository.InvoiceConfirmationRepository;
import com.example.eatable.infrastructure.database.repository.InvoiceRequestRepository;
import com.example.eatable.infrastructure.mq.model.InvoiceMessage;
import com.example.eatable.infrastructure.mq.model.mapper.InvoiceMessageMapper;
import com.example.eatable.infrastructure.restful.RestClient;
import com.example.eatable.infrastructure.restful.model.InvoiceRequest;
import com.example.eatable.infrastructure.restful.model.InvoiceResponse;
import com.example.eatable.infrastructure.restful.model.InvoiceResultResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceListenerTest {

    private InvoiceListener invoiceListener;

    private RestClient restClient;

    private RabbitTemplate rabbitTemplate;

    private InvoiceRequestRepository invoiceRequestRepository;

    private InvoiceConfirmationRepository invoiceConfirmationRepository;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        rabbitTemplate = mock(RabbitTemplate.class);
        invoiceRequestRepository = mock(InvoiceRequestRepository.class);
        invoiceConfirmationRepository = mock(InvoiceConfirmationRepository.class);
        invoiceListener = new InvoiceListener(restClient, rabbitTemplate, invoiceRequestRepository, invoiceConfirmationRepository);
    }

    @Test
    void should_send_api_request_and_send_toConfirm_message_given_200_response_when_handle_invoice_request() {
        InvoiceMessage invoiceMessage = InvoiceMessage.builder()
                .amount("2000")
                .contactId(1L)
                .createAt(LocalDateTime.now())
                .build();
        InvoiceRequest invoiceRequest = InvoiceMessageMapper.MAPPER.messageToRequest(invoiceMessage);
        InvoiceResponse invoiceResponse = InvoiceResponse.builder().nid(1L).build();
        when(restClient.post(any(), eq(invoiceRequest), eq(InvoiceResponse.class)))
                .thenReturn(new ResponseEntity<>(invoiceResponse, HttpStatus.OK));
        doNothing().when(rabbitTemplate).convertAndSend(eq(MessageConfiguration.TO_CONFIRM_QUEUE_NAME), eq(invoiceMessage));

        invoiceListener.handleRequestMessage(invoiceMessage);

        verify(restClient, times(1)).post(any(), eq(invoiceRequest), eq(InvoiceResponse.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq(MessageConfiguration.TO_CONFIRM_QUEUE_NAME), eq(invoiceMessage));

    }

    @Test
    void should_save_failed_confirmation_when_handle_invoice_request_and_message_is_invalid() {
        InvoiceMessage invoiceMessage = InvoiceMessage.builder()
                .amount("2000")
                .contactId(1L)
                .createAt(LocalDateTime.now().minusDays(3))
                .build();
        ArgumentCaptor<InvoiceConfirmationEntity> captor = ArgumentCaptor.forClass(InvoiceConfirmationEntity.class);

        when(invoiceConfirmationRepository.save(captor.capture()))
                .thenReturn(InvoiceConfirmationEntity.builder()
                        .id(1L)
                        .contactId(1L)
                        .result("failed")
                        .createAt(LocalDateTime.now()).build());

        invoiceListener.handleRequestMessage(invoiceMessage);

        verify(invoiceConfirmationRepository, times(1)).save(eq(captor.getValue()));
    }

    @Test
    void should_send_api_request_and_send_retry_message_given_timeout_request_when_handle_invoice_request() {
        InvoiceMessage invoiceMessage = InvoiceMessage.builder()
                .amount("2000")
                .contactId(1L)
                .createAt(LocalDateTime.now())
                .build();
        InvoiceRequest invoiceRequest = InvoiceMessageMapper.MAPPER.messageToRequest(invoiceMessage);
        doThrow(new RestClientException("timeout")).when(restClient).post(any(), eq(invoiceRequest), eq(InvoiceResponse.class));

        invoiceListener.handleRequestMessage(invoiceMessage);

        verify(rabbitTemplate, times(1)).convertAndSend(eq(MessageConfiguration.RETRY_QUEUE_NAME), eq(invoiceMessage));
    }

    @Test
    void should_send_api_request_and_send_toConfirm_message_given_not_200_response_when_handle_invoice_request() {
        InvoiceMessage invoiceMessage = InvoiceMessage.builder()
                .amount("2000")
                .contactId(1L)
                .createAt(LocalDateTime.now())
                .build();
        InvoiceRequest invoiceRequest = InvoiceMessageMapper.MAPPER.messageToRequest(invoiceMessage);
        when(restClient.post(any(), eq(invoiceRequest), eq(InvoiceResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE));
        doNothing().when(rabbitTemplate).convertAndSend(eq(MessageConfiguration.RETRY_QUEUE_NAME), eq(invoiceMessage));

        invoiceListener.handleRequestMessage(invoiceMessage);

        verify(restClient, times(1)).post(any(), eq(invoiceRequest), eq(InvoiceResponse.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq(MessageConfiguration.RETRY_QUEUE_NAME), eq(invoiceMessage));

    }

    @Test
    void should_send_toConfirm_message_when_handle_invoice_confirm_and_not_be_confirmed_in_local_database() {
        InvoiceMessage invoiceMessage = InvoiceMessage.builder()
                .amount("2000")
                .contactId(1L)
                .createAt(LocalDateTime.now())
                .build();
        when(invoiceConfirmationRepository.findByContactId(eq(1L))).thenReturn(Optional.empty());
        when(restClient.get(any(), eq(InvoiceResultResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        doNothing().when(rabbitTemplate).convertAndSend(eq(MessageConfiguration.TO_CONFIRM_QUEUE_NAME), eq(invoiceMessage));

        invoiceListener.handleConfirmationMessage(invoiceMessage);

        verify(invoiceConfirmationRepository, times(1)).findByContactId(eq(1L));
        verify(restClient, times(1)).get(any(), eq(InvoiceResultResponse.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq(MessageConfiguration.TO_CONFIRM_QUEUE_NAME), eq(invoiceMessage));
    }

    @Test
    void should_send_toConfirm_message_when_handle_invoice_confirm_and_has_been_confirmed_from_invoice_api() {
        InvoiceMessage invoiceMessage = InvoiceMessage.builder()
                .amount("2000")
                .contactId(1L)
                .createAt(LocalDateTime.now())
                .build();
        when(invoiceConfirmationRepository.findByContactId(eq(1L))).thenReturn(Optional.empty());
        when(restClient.get(any(), any()))
                .thenReturn(new ResponseEntity<>(InvoiceResultResponse.builder().result("success").build(), HttpStatus.OK));
        when(invoiceConfirmationRepository.save(any())).thenReturn(InvoiceConfirmationEntity.builder()
                .result("success")
                .contactId(1L)
                .id(1L)
                .createAt(LocalDateTime.now())
                .build());

        invoiceListener.handleConfirmationMessage(invoiceMessage);

        verify(invoiceConfirmationRepository, times(1)).findByContactId(eq(1L));
        verify(restClient, times(1)).get(any(), eq(InvoiceResultResponse.class));
        verify(invoiceConfirmationRepository, times(1)).save(any());
    }

    @Test
    void should_not_send_toConfirm_message_when_handle_invoice_confirm_and_has_been_confirmed_in_local_database() {
        InvoiceMessage invoiceMessage = InvoiceMessage.builder()
                .amount("2000")
                .contactId(1L)
                .createAt(LocalDateTime.now())
                .build();
        InvoiceConfirmationEntity invoiceConfirmationEntity = InvoiceConfirmationEntity.builder()
                .id(1L)
                .contactId(1L)
                .result("success")
                .createAt(LocalDateTime.now())
                .build();
        when(invoiceConfirmationRepository.findByContactId(eq(1L))).thenReturn(Optional.of(invoiceConfirmationEntity));

        invoiceListener.handleConfirmationMessage(invoiceMessage);

        verify(invoiceConfirmationRepository, times(1)).findByContactId(eq(1L));
        verify(rabbitTemplate, times(0)).convertAndSend(eq(MessageConfiguration.TO_CONFIRM_QUEUE_NAME), eq(invoiceMessage));
    }

    @Test
    void should_save_failed_confirmation_when_handle_invoice_confirmation_and_message_is_invalid() {
        InvoiceMessage invoiceMessage = InvoiceMessage.builder()
                .amount("2000")
                .contactId(1L)
                .createAt(LocalDateTime.now().minusDays(3))
                .build();
        ArgumentCaptor<InvoiceConfirmationEntity> captor = ArgumentCaptor.forClass(InvoiceConfirmationEntity.class);

        when(invoiceConfirmationRepository.save(captor.capture()))
                .thenReturn(InvoiceConfirmationEntity.builder()
                        .id(1L)
                        .contactId(1L)
                        .result("failed")
                        .createAt(LocalDateTime.now()).build());

        invoiceListener.handleConfirmationMessage(invoiceMessage);

        verify(invoiceConfirmationRepository, times(1)).save(eq(captor.getValue()));
    }
}
