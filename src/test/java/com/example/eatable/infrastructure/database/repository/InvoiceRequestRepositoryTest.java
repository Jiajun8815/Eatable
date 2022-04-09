package com.example.eatable.infrastructure.database.repository;

import com.example.eatable.infrastructure.database.entity.InvoiceRequestEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
class InvoiceRequestRepositoryTest {
    @Autowired
    private InvoiceRequestRepository invoiceRequestRepository;

    @AfterEach
    void tearDown() {
        invoiceRequestRepository.deleteAll();
    }

    @Test
    public void should_get_correct_entity_when_retrieve_from_database() {
        InvoiceRequestEntity original = InvoiceRequestEntity.builder()
                .amount("2000")
                .contactId(1)
                .createAt(LocalDateTime.now())
                .build();

        InvoiceRequestEntity result = invoiceRequestRepository.save(original);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(original);

    }
}
