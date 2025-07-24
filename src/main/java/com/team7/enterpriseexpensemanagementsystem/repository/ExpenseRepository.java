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

    @Query("SELECT EXTRACT(MONTH FROM e.expenseDate) as month, SUM(e.amount) as total " +
           "FROM Expense e " +
           "WHERE (:userId IS NULL OR e.user.id = :userId) " +
           "AND (:start IS NULL OR e.expenseDate >= :start) " +
           "AND (:end IS NULL OR e.expenseDate <= :end) " +
           "GROUP BY EXTRACT(MONTH FROM e.expenseDate) " +
           "ORDER BY EXTRACT(MONTH FROM e.expenseDate)")
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
      AND e.expenseDate >= COALESCE(:startDate, e.expenseDate)
      AND e.expenseDate <= COALESCE(:endDate, e.expenseDate)
    GROUP BY a.status
""")
    List<Object[]> getStatusAnalytics(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

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

    @Query(value = """
        SELECT
            TRIM(TO_CHAR(e.expense_date, 'Day')),
            SUM(e.amount),
            EXTRACT(ISODOW FROM e.expense_date)
        FROM
            expenses e
        WHERE
            e.user_id = :userId AND e.expense_date BETWEEN :start AND :end
        GROUP BY
            1, 3
        ORDER BY
            3
    """, nativeQuery = true)
    List<Object[]> findTotalByDayOfWeekForUser(@Param("start") LocalDate start,
                                               @Param("end") LocalDate end,
                                               @Param("userId") Long userId);
}