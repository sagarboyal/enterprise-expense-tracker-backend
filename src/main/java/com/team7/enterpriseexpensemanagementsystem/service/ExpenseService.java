package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.dto.ExpenseDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;

public interface ExpenseService {
    ExpenseDTO addExpense(ExpenseDTO dto);
    ExpenseDTO updateExpense(ExpenseDTO dto);
    void deleteExpense(Long id);
    ExpenseDTO getExpenseById(Long id);
    ExpenseResponse getAllExpenses(Integer pageNumber,Integer pageSize, String sortBy, String sortOrder);
    ExpenseResponse getExpensesByCategoryName(String categoryName,
                                              Integer pageNumber,Integer pageSize, String sortBy, String sortOrder);

}

