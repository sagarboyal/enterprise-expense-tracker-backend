package com.main.trex.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryExpenseDTO {
    private String category;
    private Double total;
}


