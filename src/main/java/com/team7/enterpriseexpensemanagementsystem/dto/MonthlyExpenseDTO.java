package com.team7.enterpriseexpensemanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MonthlyExpenseDTO {
    private String month;
    private Double total;
}
