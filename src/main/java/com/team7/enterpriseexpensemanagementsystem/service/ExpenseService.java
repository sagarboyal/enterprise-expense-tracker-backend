package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.dto.ExpenseDTO;
import com.team7.enterpriseexpensemanagementsystem.dto.MonthlyExpenseDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.request.ApprovalRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.ExpenseUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    ExpenseResponse addExpense(ExpenseDTO dto, String email);
    ExpenseResponse updateExpense(ExpenseUpdateRequest dto);
    void deleteExpense(Long id);
    ExpenseResponse getExpenseByExpenseId(Long id);
    ExpenseResponse updateExpenseStatus(Long id, ApprovalRequest approvalRequest, String email);
    PagedResponse<ExpenseResponse> getFilteredExpenses(String categoryName, String status, LocalDate startDate, LocalDate endDate, Double minAmount, Double maxAmount, Long userId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    List<MonthlyExpenseDTO> getMonthlyAnalytics(Long id, LocalDate startDate, LocalDate endDate);
}

