package com.example.eatable.service;

import com.example.eatable.configuration.MessageConfiguration;
import com.example.eatable.infrastructure.database.entity.InvoiceConfirmationEntity;
import com.example.eatable.infrastructure.database.entity.InvoiceRequestEntity;
import com.example.eatable.infrastructure.database.repository.InvoiceConfirmationRepository;
import com.example.eatable.infrastructure.database.repository.InvoiceRequestRepository;
import com.example.eatable.infrastructure.mq.model.InvoiceMessage;
import com.example.eatable.model.InvoiceConfirmInfo;
import com.example.eatable.model.InvoiceRequestInfo;
import com.example.eatable.model.mapper.InvoiceMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    private static InvoiceService invoiceService;
    private static RabbitTemplate rabbitTemplate;
    private static InvoiceRequestRepository invoiceRequestRepository;
    private static InvoiceConfirmationRepository invoiceConfirmationRepository;

    @BeforeAll
    static void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        invoiceRequestRepository = mock(InvoiceRequestRepository.class);
        invoiceConfirmationRepository = mock(InvoiceConfirmationRepository.class);
        invoiceService = new InvoiceService(rabbitTemplate, invoiceRequestRepository, invoiceConfirmationRepository);
    }

    @Test
    public void should_send_request_message_and_save_request_when_call_request_invoice() {
        InvoiceRequestInfo invoiceRequestInfo = InvoiceRequestInfo.builder()
                .contactId(1L)
                .amount("2000")
                .createAt(LocalDateTime.now())
                .build();
        InvoiceMessage invoiceMessage = InvoiceMapper.MAPPER.requestModelToMessage(invoiceRequestInfo);
        InvoiceRequestEntity invoiceRequestEntity = InvoiceMapper.MAPPER.requestModelToRequestEntity(invoiceRequestInfo);
        doNothing().when(rabbitTemplate).convertAndSend(eq(MessageConfiguration.REQUEST_QUEUE_NAME), eq(invoiceMessage));
        when(invoiceRequestRepository.save(eq(invoiceRequestEntity))).thenReturn(InvoiceRequestEntity.builder()
                .id(1L)
                .contactId(1L)
                .amount("2000")
                .createAt(LocalDateTime.now()).build());

        invoiceService.requestInvoice(invoiceRequestInfo);

        verify(rabbitTemplate, times(1)).convertAndSend(eq(MessageConfiguration.REQUEST_QUEUE_NAME), eq(invoiceMessage));
        verify(invoiceRequestRepository, times(1)).save(eq(invoiceRequestEntity));

    }

    @Test
    public void should_save_confirmation_when_call_confirm_invoice() {
        InvoiceConfirmInfo invoiceConfirmInfo = InvoiceConfirmInfo.builder()
                .contactId(1L)
                .result("success")
                .createAt(LocalDateTime.now())
                .build();
        InvoiceConfirmationEntity invoiceConfirmationEntity = InvoiceMapper.MAPPER.confirmModelToConfirmEntity(invoiceConfirmInfo);
        when(invoiceConfirmationRepository.save(eq(invoiceConfirmationEntity))).thenReturn(InvoiceConfirmationEntity.builder()
                .id(1L)
                .contactId(1L)
                .result("success")
                .createAt(LocalDateTime.now()).build());

        invoiceService.confirmInvoice(invoiceConfirmInfo);

        verify(invoiceConfirmationRepository, times(1)).save(eq(invoiceConfirmationEntity));

    }

    @Test
    public void should_get_confirmation_given_exist_confirmation_contact_id_when_call_get_confirmation() {
        InvoiceConfirmationEntity invoiceConfirmationEntity = InvoiceConfirmationEntity.builder()
                .id(1L)
                .contactId(1L)
                .result("test")
                .createAt(LocalDateTime.now())
                .build();

        when(invoiceConfirmationRepository.findByContactId(eq(1L))).thenReturn(Optional.of(invoiceConfirmationEntity));

        String result = invoiceService.getConfirmation(1L);

        assertEquals(invoiceConfirmationEntity.getResult(), result);
    }

    @Test
    public void should_get_in_progress_given_not_exist_confirmation_contact_id_when_call_get_confirmation() {
        when(invoiceConfirmationRepository.findByContactId(eq(1L))).thenReturn(Optional.empty());

        String result = invoiceService.getConfirmation(1L);

        assertEquals("in progress", result);
    }

}
