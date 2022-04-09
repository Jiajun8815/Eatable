package com.example.eatable.infrastructure.database.repository;

import com.example.eatable.infrastructure.database.entity.InvoiceRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRequestRepository extends JpaRepository<InvoiceRequestEntity, Long> {
    Optional<InvoiceRequestEntity> findByContactId(Long contactId);

}
