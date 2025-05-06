package com.team7.enterpriseexpensemanagementsystem.payload.request;

import lombok.Data;

@Data
public class SignUpRequest {
    private String fullName;
    private String email;
    private String password;
}
