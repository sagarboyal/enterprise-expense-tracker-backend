package com.team7.enterpriseexpensemanagementsystem.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApprovalRequest {
    @NotNull(message = "Decision must be either APPROVE or REJECT.")
    private String decision;
    private String message;
}
