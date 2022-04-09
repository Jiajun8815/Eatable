package com.example.eatable.controller;

import com.example.eatable.dto.InvoiceRequestDTO;
import com.example.eatable.model.InvoiceConfirmInfo;
import com.example.eatable.model.InvoiceRequestInfo;
import com.example.eatable.service.InvoiceService;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/service-contracts/{id}/invoice")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public ResponseEntity<Void> requestInvoice(@PathVariable Long id, RequestEntity<InvoiceRequestDTO> request) {
        InvoiceRequestDTO invoiceRequestDTO = request.getBody();
        InvoiceRequestInfo invoiceRequestInfo = InvoiceRequestInfo.builder()
                .contactId(id)
                .amount(invoiceRequestDTO.getAmount())
                .createAt(LocalDateTime.now())
                .build();

        invoiceService.requestInvoice(invoiceRequestInfo);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirmation")
    public ResponseEntity<Void> confirmInvoice(@PathVariable Long id, RequestEntity<InvoiceConfirmInfo> request) {
        InvoiceConfirmInfo invoiceConfirmDTO = request.getBody();
        InvoiceConfirmInfo invoiceConfirmInfo = InvoiceConfirmInfo.builder()
                .contactId(id)
                .result(invoiceConfirmDTO.getResult())
                .createAt(LocalDateTime.now())
                .build();

        invoiceService.confirmInvoice(invoiceConfirmInfo);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/confirmation")
    public ResponseEntity<String> getConfirmation(@PathVariable Long id) {
        String confirmation = invoiceService.getConfirmation(id);

        return ResponseEntity.ok(confirmation);
    }
}
