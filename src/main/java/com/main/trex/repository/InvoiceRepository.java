package com.main.trex.repository;

import com.main.trex.entity.Invoice;
import com.main.trex.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {
    Invoice findByUserIdAndStatus(Long userId, InvoiceStatus status);
    Optional<Invoice> findTopByUserIdAndStatusOrderByGeneratedAtDesc(Long userId, InvoiceStatus status);
    void deleteByUserId(Long userId);
}
