package com.team7.enterpriseexpensemanagementsystem.repository;

import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal getTotalExpensesByUserId(@Param("userId") Long userId);

    @Query("SELECT MONTH(e.expenseDate) as month, SUM(e.amount) as total " +
            "FROM Expense e " +
            "WHERE (:userId IS NULL OR e.user.id = :userId) " +
            "AND (:start IS NULL OR e.expenseDate >= :start) " +
            "AND (:end IS NULL OR e.expenseDate <= :end) " +
            "GROUP BY MONTH(e.expenseDate) " +
            "ORDER BY MONTH(e.expenseDate)")
    List<Object[]> getMonthlyExpenseTotals(@Param("userId") Long userId,
                                           @Param("start") LocalDate start,
                                           @Param("end") LocalDate end);
}
