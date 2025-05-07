package com.team7.enterpriseexpensemanagementsystem.payload.response;

import com.team7.enterpriseexpensemanagementsystem.dto.ExpenseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseResponse {
    private List<ExpenseDTO> expenses;
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private boolean lastPage;
}

