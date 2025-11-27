package com.main.trex.service;

import com.main.trex.dto.*;
import com.main.trex.entity.Approval;
import com.main.trex.payload.request.ApprovalRequest;
import com.main.trex.payload.request.ExpenseUpdateRequest;
import com.main.trex.payload.response.ExpenseResponse;
import com.main.trex.payload.response.PagedResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    ExpenseResponse addExpense(ExpenseDTO dto, String email);
    ExpenseResponse updateExpense(ExpenseUpdateRequest dto);
    void deleteExpense(Long id);
    ExpenseResponse getExpenseByExpenseId(Long id);
    ExpenseResponse updateExpenseStatus(Long id, ApprovalRequest approvalRequest, String email);
    PagedResponse<ExpenseResponse> getFilteredExpenses(String title, String categoryName, String status, String level, LocalDate startDate, LocalDate endDate, Double minAmount, Double maxAmount, Long userId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, Boolean export, HttpServletResponse response);
    List<MonthlyExpenseDTO> getMonthlyAnalytics(Long id, LocalDate startDate, LocalDate endDate);
    List<CategoryExpenseDTO> getCategoryAnalytics(Long id, LocalDate startDate, LocalDate endDate);
    List<StatusExpenseDTO> getStatusAnalytics(Long id, LocalDate startDate, LocalDate endDate);
    SummaryDTO getSummary(Long id);
    void exportFilteredExpenses(
            List<ExpenseResponse> expenseList,
            HttpServletResponse response
    ) throws IOException;
    List<ExpenseDTO> saveAll(List<ExpenseDTO> expenses);
    List<Approval> getApprovalStack(Long expenseId);
    List<WeeklyExpenseDTO> getWeeklyAnalytics();
}

