package com.team7.enterpriseexpensemanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseDTO {
    private Long id;
    private String title;
    private Double amount;
    private LocalDate expenseDate;
    private Long categoryId;  // link to Category
}

