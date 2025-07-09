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

    @Query("""
    SELECT a.status, SUM(e.amount)
    FROM Expense e
    JOIN e.approvals a
    WHERE e.user.id = :userId
      AND a.actionTime = (
          SELECT MAX(a2.actionTime)
          FROM Approval a2
          WHERE a2.expense.id = e.id
      )
      AND (:startDate IS NULL OR e.expenseDate >= :startDate)
      AND (:endDate IS NULL OR e.expenseDate <= :endDate)
    GROUP BY a.status
""")
    List<Object[]> getStatusAnalytics(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );



    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
    Double getTotalExpensesByUser(@Param("userId") Long userId);

    @Query("""
    SELECT COUNT(e)
    FROM Expense e
    JOIN e.approvals a
    WHERE e.user.id = :userId
      AND a.actionTime = (
          SELECT MAX(a2.actionTime)
          FROM Approval a2
          WHERE a2.expense.id = e.id
      )
      AND a.status = com.team7.enterpriseexpensemanagementsystem.entity.ApprovalStatus.APPROVED
""")
    Long countApprovedExpenses(@Param("userId") Long userId);

    @Query("""
    SELECT COUNT(e)
    FROM Expense e
    JOIN e.approvals a
    WHERE e.user.id = :userId
      AND a.actionTime = (
          SELECT MAX(a2.actionTime)
          FROM Approval a2
          WHERE a2.expense.id = e.id
      )
      AND a.status = com.team7.enterpriseexpensemanagementsystem.entity.ApprovalStatus.REJECTED
""")
    Long countRejectedExpenses(@Param("userId") Long userId);



    @Query("""
    SELECT COUNT(e)
    FROM Expense e
    JOIN e.approvals a
    WHERE e.user.id = :userId
      AND a.actionTime = (
          SELECT MAX(a2.actionTime)
          FROM Approval a2
          WHERE a2.expense.id = e.id
      )
      AND a.status = com.team7.enterpriseexpensemanagementsystem.entity.ApprovalStatus.PENDING
""")
    Long countPendingApprovals(@Param("userId") Long userId);

    @Query("""
    SELECT FUNCTION('DAYNAME', e.expenseDate), SUM(e.amount), FUNCTION('DAYOFWEEK', e.expenseDate)
    FROM Expense e
    WHERE e.expenseDate BETWEEN :start AND :end
    GROUP BY FUNCTION('DAYNAME', e.expenseDate), FUNCTION('DAYOFWEEK', e.expenseDate)
    ORDER BY FUNCTION('DAYOFWEEK', e.expenseDate)
""")
    List<Object[]> findTotalByDayOfWeek(@Param("start") LocalDate start,
                                        @Param("end") LocalDate end);


    void deleteByUserId(Long userId);
}
