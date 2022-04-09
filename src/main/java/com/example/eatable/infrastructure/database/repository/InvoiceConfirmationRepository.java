package com.example.eatable.infrastructure.database.repository;

import com.example.eatable.infrastructure.database.entity.InvoiceConfirmationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceConfirmationRepository extends JpaRepository<InvoiceConfirmationEntity, Long> {

    Optional<InvoiceConfirmationEntity> findByContactId(Long contactId);

}
