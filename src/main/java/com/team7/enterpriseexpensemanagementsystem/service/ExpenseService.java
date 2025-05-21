package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.dto.ExpenseDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.request.ApprovalRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.ExpenseUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpensePagedResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;

public interface ExpenseService {
    ExpenseResponse addExpense(ExpenseDTO dto, String email);
    ExpenseResponse updateExpense(ExpenseUpdateRequest dto);
    void deleteExpense(Long id);
    ExpenseResponse getExpenseByExpenseId(Long id);
    ExpensePagedResponse getAllExpenses(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    ExpensePagedResponse getExpensesByCategoryName(String categoryName,
                                                   Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    ExpenseResponse updateExpenseStatus(Long id, ApprovalRequest approvalRequest, String email);
}

