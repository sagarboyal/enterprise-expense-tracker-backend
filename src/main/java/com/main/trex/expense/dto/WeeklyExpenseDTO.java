package com.main.trex.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class WeeklyExpenseDTO {
    private String day;
    private Double total;
}



