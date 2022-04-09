package com.example.eatable.infrastructure.restful.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class InvoiceRequest {
    private Long contactId;
    private String amount;
}
