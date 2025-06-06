package com.team7.enterpriseexpensemanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryDTO {
    private Double totalExpenses;
    private Long approvedCountThisMonth;
    private Long pendingApprovals;
    private List<StatusExpenseDTO> statusAnalytics;
}
