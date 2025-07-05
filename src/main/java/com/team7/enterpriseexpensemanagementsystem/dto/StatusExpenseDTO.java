package com.team7.enterpriseexpensemanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatusExpenseDTO {
    private String status;
    private Double total;

    public StatusExpenseDTO(String status, Double total) {
        this.status = status;
        this.total = total;
    }
}
