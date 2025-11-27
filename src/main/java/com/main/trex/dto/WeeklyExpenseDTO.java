package com.main.trex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class WeeklyExpenseDTO {
    private String day;
    private Double total;
}

