package com.team7.enterpriseexpensemanagementsystem.repository;

import com.team7.enterpriseexpensemanagementsystem.dto.StatusExpenseDTO;
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

    @Query("SELECT e.category.name as category, SUM(e.amount) as total " +
            "FROM Expense e " +
            "WHERE (:userId IS NULL OR e.user.id = :userId) " +
            "AND (:start IS NULL OR e.expenseDate >= :start) " +
            "AND (:end IS NULL OR e.expenseDate <= :end) " +
            "GROUP BY e.category.name " +
            "ORDER BY total DESC")
    List<Object[]> getCategoryExpenseTotals(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
    @Query("SELECT e.status as status, SUM(e.amount) as total " +
            "FROM Expense e " +
            "WHERE (:userId IS NULL OR e.user.id = :userId) " +
            "AND (:startDate IS NULL OR e.expenseDate >= :startDate) " +
            "AND (:endDate IS NULL OR e.expenseDate <= :endDate) " +
            "GROUP BY e.status")
    List<Object[]> getStatusAnalytics(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
    Double getTotalExpensesByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId " +
            "AND (e.status = com.team7.enterpriseexpensemanagementsystem.entity.Approval.APPROVED_BY_MANAGER " +
            "OR e.status = com.team7.enterpriseexpensemanagementsystem.entity.Approval.APPROVED_BY_ADMIN) " +
            "AND FUNCTION('MONTH', e.expenseDate) = :month " +
            "AND FUNCTION('YEAR', e.expenseDate) = :year")
    Long countApprovedThisMonth(@Param("userId") Long userId,
                                @Param("month") int month,
                                @Param("year") int year);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId AND e.status = com.team7.enterpriseexpensemanagementsystem.entity.Approval.PENDING")
    Long countPendingApprovals(@Param("userId") Long userId);
}
