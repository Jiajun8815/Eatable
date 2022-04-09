package com.example.eatable.model.mapper;


import com.example.eatable.infrastructure.database.entity.InvoiceConfirmationEntity;
import com.example.eatable.infrastructure.database.entity.InvoiceRequestEntity;
import com.example.eatable.infrastructure.mq.model.InvoiceMessage;
import com.example.eatable.model.InvoiceConfirmInfo;
import com.example.eatable.model.InvoiceRequestInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InvoiceMapper {
    InvoiceMapper MAPPER = Mappers.getMapper(
            InvoiceMapper.class
    );

    InvoiceMessage requestModelToMessage(InvoiceRequestInfo invoiceRequestInfo);

    @Mapping(target = "id", ignore = true)
    InvoiceRequestEntity requestModelToRequestEntity(InvoiceRequestInfo invoiceRequestInfo);

    @Mapping(target = "id", ignore = true)
    InvoiceConfirmationEntity confirmModelToConfirmEntity(InvoiceConfirmInfo invoiceRequestInfo);

}
