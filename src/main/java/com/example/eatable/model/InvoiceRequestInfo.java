package com.example.eatable.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InvoiceRequestInfo {
    private long contactId;

    private String amount;

    private LocalDateTime createAt;
}
