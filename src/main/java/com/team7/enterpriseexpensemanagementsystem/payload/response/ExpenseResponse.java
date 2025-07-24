package com.team7.enterpriseexpensemanagementsystem.payload.response;

import com.team7.enterpriseexpensemanagementsystem.entity.ApprovalLevel;
import com.team7.enterpriseexpensemanagementsystem.entity.ApprovalStatus;
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
    private Long userId;
    private String fullName;
    private String title;
    private Double amount;
    private LocalDate expenseDate;
    private String category;
    private String description;
    private ApprovalStatus status;
    private ApprovalLevel level;
    private String message;
}
