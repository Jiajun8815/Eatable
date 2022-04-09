package com.example.eatable.infrastructure.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "InvoiceRequest")
public class InvoiceRequestEntity {


    @Id
    @GeneratedValue(strategy = SEQUENCE)
    private Long id;

    private long contactId;

    private String amount;

    private LocalDateTime createAt;


}
