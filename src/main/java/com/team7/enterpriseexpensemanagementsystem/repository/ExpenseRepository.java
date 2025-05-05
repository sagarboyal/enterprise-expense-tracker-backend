package com.team7.enterpriseexpensemanagementsystem.repository;

import com.team7.enterpriseexpensemanagementsystem.entity.Category;
import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Page<Expense> findByCategory(Category category, Pageable pageable);
}
