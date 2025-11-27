package com.main.trex.repository;

import com.main.trex.entity.FileDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileDocumentRepository extends JpaRepository<FileDocument, Long> {
    Optional<FileDocument> findByExpenseId(Long expenseId);
}
