package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.dto.ExpenseDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface ExpenseService {
    ExpenseDTO addExpense(ExpenseDTO dto, UserDetails userDetails);
    ExpenseDTO updateExpense(ExpenseDTO dto, UserDetails userDetails);
    void deleteExpense(Long id);
    ExpenseDTO getExpenseById(Long id);
    ExpenseResponse getAllExpenses(Integer pageNumber,Integer pageSize, String sortBy, String sortOrder);
    ExpenseResponse getExpensesByCategoryName(String categoryName,
                                              Integer pageNumber,Integer pageSize, String sortBy, String sortOrder);

}

