package com.main.trex.expense.payload.response;

import com.main.trex.expense.entity.ApprovalLevel;
import com.main.trex.expense.entity.ApprovalStatus;
import com.main.trex.expense.entity.ExpenseWorkspaceType;
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
    private ExpenseWorkspaceType workspaceType;
    private Long organizationId;
    private String organizationName;
}


