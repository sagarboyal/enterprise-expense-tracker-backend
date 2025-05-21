package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.dto.ExpenseDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpensePagedResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;

public interface ExpenseService {
    ExpenseResponse addExpense(ExpenseDTO dto, String email);
    ExpenseDTO updateExpense(ExpenseDTO dto);
    void deleteExpense(Long id);
    ExpenseDTO getExpenseById(Long id);
    ExpensePagedResponse getAllExpenses(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    ExpensePagedResponse getExpensesByCategoryName(String categoryName,
                                                   Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

}

