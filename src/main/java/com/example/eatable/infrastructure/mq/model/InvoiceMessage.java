package com.example.eatable.infrastructure.mq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceMessage {
    private long contactId;
    private String amount;
    private LocalDateTime createAt;
}
