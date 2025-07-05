package com.team7.enterpriseexpensemanagementsystem.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SignInResponse {
    private String username;
    private String token;
}
