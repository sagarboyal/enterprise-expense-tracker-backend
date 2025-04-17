package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.payload.expense.ExpenseDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.expense.ExpenseResponse;

public interface ExpenseService {
    ExpenseDTO addExpense(ExpenseDTO dto);
    ExpenseDTO updateExpense(ExpenseDTO dto);
    void deleteExpense(Long id);
    ExpenseDTO getExpenseById(Long id);
    ExpenseResponse getAllExpenses();
    ExpenseResponse getExpensesByCategoryName(String categoryName);

}

