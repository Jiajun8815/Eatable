package com.example.eatable.service;

import com.example.eatable.configuration.MessageConfiguration;
import com.example.eatable.infrastructure.database.entity.InvoiceConfirmationEntity;
import com.example.eatable.infrastructure.database.repository.InvoiceConfirmationRepository;
import com.example.eatable.infrastructure.database.repository.InvoiceRequestRepository;
import com.example.eatable.model.InvoiceConfirmInfo;
import com.example.eatable.model.InvoiceRequestInfo;
import com.example.eatable.model.mapper.InvoiceMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {
    private final RabbitTemplate rabbitTemplate;

    private final InvoiceRequestRepository invoiceRequestRepository;

    private final InvoiceConfirmationRepository invoiceConfirmationRepository;

    public InvoiceService(RabbitTemplate rabbitTemplate, InvoiceRequestRepository invoiceRequestRepository, InvoiceConfirmationRepository invoiceConfirmationRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.invoiceRequestRepository = invoiceRequestRepository;
        this.invoiceConfirmationRepository = invoiceConfirmationRepository;
    }

    public void requestInvoice(InvoiceRequestInfo invoiceRequestInfo) {
        rabbitTemplate.convertAndSend(MessageConfiguration.REQUEST_QUEUE_NAME, InvoiceMapper.MAPPER.requestModelToMessage(invoiceRequestInfo));
        invoiceRequestRepository.save(InvoiceMapper.MAPPER.requestModelToRequestEntity(invoiceRequestInfo));
    }

    public void confirmInvoice(InvoiceConfirmInfo invoiceConfirmInfo) {
        invoiceConfirmationRepository.save(InvoiceMapper.MAPPER.confirmModelToConfirmEntity(invoiceConfirmInfo));
    }

    public String getConfirmation(Long contactId) {
        return invoiceConfirmationRepository.findByContactId(contactId).map(InvoiceConfirmationEntity::getResult).orElse("in progress");
    }
}
