package com.main.trex.repository;

import com.main.trex.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    void deleteByUserId(Long userId);
}
