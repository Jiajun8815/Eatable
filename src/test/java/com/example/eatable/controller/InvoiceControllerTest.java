package com.example.eatable.controller;

import com.example.eatable.dto.InvoiceConfirmDTO;
import com.example.eatable.dto.InvoiceRequestDTO;
import com.example.eatable.infrastructure.database.repository.InvoiceConfirmationRepository;
import com.example.eatable.infrastructure.database.repository.InvoiceRequestRepository;
import com.example.eatable.model.InvoiceConfirmInfo;
import com.example.eatable.model.InvoiceRequestInfo;
import com.example.eatable.service.InvoiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class InvoiceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InvoiceRequestRepository invoiceRequestRepository;

    @Autowired
    private InvoiceConfirmationRepository invoiceConfirmationRepository;

    @MockBean
    private InvoiceService invoiceService;

    @AfterEach
    void tearDown() {
        invoiceRequestRepository.deleteAll();
        invoiceConfirmationRepository.deleteAll();
    }

    @SneakyThrows
    @Test
    void shoud_get_200_when_call_post_invoice_endpoint() {
        ArgumentCaptor<InvoiceRequestInfo> captor = ArgumentCaptor.forClass(InvoiceRequestInfo.class);
        doNothing().when(invoiceService).requestInvoice(captor.capture());

        mockMvc.perform(
                        post("/service-contracts/1/invoice")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(InvoiceRequestDTO.builder()
                                        .amount("2000")
                                        .build()))
                )
                .andExpect(status().isOk());
        InvoiceRequestInfo invoiceRequestInfo = captor.getValue();

        verify(invoiceService).requestInvoice(eq(invoiceRequestInfo));
        assertEquals(1L, invoiceRequestInfo.getContactId());
        assertEquals("2000", invoiceRequestInfo.getAmount());

    }

    @SneakyThrows
    @Test
    void shoud_get_200_when_call_post_invoice_confirmation_endpoint() {
        ArgumentCaptor<InvoiceConfirmInfo> captor = ArgumentCaptor.forClass(InvoiceConfirmInfo.class);
        doNothing().when(invoiceService).confirmInvoice(captor.capture());

        mockMvc.perform(
                        post("/service-contracts/1/invoice/confirmation")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(InvoiceConfirmDTO.builder()
                                        .result("success")
                                        .build()))
                )
                .andExpect(status().isOk());
        InvoiceConfirmInfo invoiceConfirmInfo = captor.getValue();

        verify(invoiceService).confirmInvoice(eq(invoiceConfirmInfo));
        assertEquals(1L, invoiceConfirmInfo.getContactId());
        assertEquals("success", invoiceConfirmInfo.getResult());
    }

    @SneakyThrows
    @Test
    void should_get_result_when_call_get_invoice_confirmation_endpoint() {
        when(invoiceService.getConfirmation(1L)).thenReturn("success");

        mockMvc.perform(
                        get("/service-contracts/1/invoice/confirmation")
                                .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

    }
}
