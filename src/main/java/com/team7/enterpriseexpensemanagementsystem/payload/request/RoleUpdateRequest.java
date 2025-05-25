package com.team7.enterpriseexpensemanagementsystem.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class RoleUpdateRequest {
    @NotBlank(message = "Role must not be blank (choose either 'EMPLOYEE' or 'MANAGER' or 'ADMIN')")
    private String role;

    @NotBlank(message = "Action must not be blank (choose either 'PROMOTE' or 'DEMOTE')")
    private String action;
}

