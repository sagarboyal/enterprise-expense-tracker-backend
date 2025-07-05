package com.team7.enterpriseexpensemanagementsystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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
    @NotBlank(message = "Title must not be empty")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private Double amount;


    @PastOrPresent(message = "Expense date cannot be in the future")
    private LocalDate expenseDate;
    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}

