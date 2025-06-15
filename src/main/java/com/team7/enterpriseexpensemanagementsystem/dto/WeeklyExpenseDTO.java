package com.team7.enterpriseexpensemanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class WeeklyExpenseDTO {
    private String day;
    private Double total;
}

