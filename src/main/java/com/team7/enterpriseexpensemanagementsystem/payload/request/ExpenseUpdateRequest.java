package com.team7.enterpriseexpensemanagementsystem.payload.request;

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
public class ExpenseUpdateRequest {
    @NotNull(message = "Id is required")
    private Long id;
    private String title;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private Double amount;

    @PastOrPresent(message = "Expense date cannot be in the future")
    private LocalDate expenseDate;
    private String description;

    private Long categoryId;
}
