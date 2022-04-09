package com.example.eatable.infrastructure.mq.model.mapper;

import com.example.eatable.infrastructure.mq.model.InvoiceMessage;
import com.example.eatable.infrastructure.restful.model.InvoiceRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InvoiceMessageMapper {
    InvoiceMessageMapper MAPPER = Mappers.getMapper(
            InvoiceMessageMapper.class
    );

    InvoiceRequest messageToRequest(InvoiceMessage invoiceMessage);
}
