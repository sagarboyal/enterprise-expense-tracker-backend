package com.team7.enterpriseexpensemanagementsystem.repository;

import com.team7.enterpriseexpensemanagementsystem.entity.Invoice;
import com.team7.enterpriseexpensemanagementsystem.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Invoice findByUserIdAndStatus(Long userId, InvoiceStatus status);
    Optional<Invoice> findTopByUserIdAndStatusOrderByGeneratedAtDesc(Long userId, InvoiceStatus status);

}
