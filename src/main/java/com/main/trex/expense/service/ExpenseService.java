package com.main.trex.expense.service;

import com.main.trex.expense.dto.ExpenseDTO;
import com.main.trex.expense.entity.Approval;
import com.main.trex.expense.payload.request.ApprovalRequest;
import com.main.trex.expense.payload.request.ExpenseUpdateRequest;
import com.main.trex.expense.payload.response.ExpenseResponse;
import com.main.trex.shared.payload.response.PagedResponse;
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
    PagedResponse<ExpenseResponse> getApprovalQueue(String title, String categoryName, String status, String level, LocalDate startDate, LocalDate endDate, Double minAmount, Double maxAmount, Long userId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, Boolean export, HttpServletResponse response);
    void exportFilteredExpenses(
            List<ExpenseResponse> expenseList,
            HttpServletResponse response
    ) throws IOException;
    List<ExpenseDTO> saveAll(List<ExpenseDTO> expenses);
    List<Approval> getApprovalStack(Long expenseId);
}



