package com.team7.enterpriseexpensemanagementsystem.payload.response;

import com.team7.enterpriseexpensemanagementsystem.entity.Approval;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {
    private Long id;
    private String title;
    private Double amount;
    private LocalDate expenseDate;
    private String category;
    private Approval status;
    private String message;
}
