package com.example.eatable.infrastructure.database.repository;

import com.example.eatable.infrastructure.database.entity.InvoiceConfirmationEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
class InvoiceConfirmationRepositoryTest {
    @Autowired
    private InvoiceConfirmationRepository invoiceConfirmationRepository;

    @AfterEach
    void tearDown() {
        invoiceConfirmationRepository.deleteAll();
    }

    @Test
    public void should_save_entity_correctly_when_save_entity() {
        InvoiceConfirmationEntity original = InvoiceConfirmationEntity.builder()
                .contactId(1)
                .result("success")
                .createAt(LocalDateTime.now())
                .build();

        InvoiceConfirmationEntity result = invoiceConfirmationRepository.save(original);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(original);

    }

    @Test
    public void should_get_correct_entity_when_retrieve_from_database() {
        InvoiceConfirmationEntity original = InvoiceConfirmationEntity.builder()
                .contactId(1)
                .result("success")
                .createAt(LocalDateTime.now())
                .build();

        invoiceConfirmationRepository.save(original);

        InvoiceConfirmationEntity result = invoiceConfirmationRepository.findByContactId(1L).get();

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(original);

    }

}
