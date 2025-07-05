package com.team7.enterpriseexpensemanagementsystem.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRequest {
    @NotNull(message = "id can't be null")
    private Long id;
    private String fullName;
    private String email;
    private String password;
}

