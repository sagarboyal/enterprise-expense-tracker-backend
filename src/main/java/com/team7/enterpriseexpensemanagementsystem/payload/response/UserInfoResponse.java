package com.team7.enterpriseexpensemanagementsystem.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String fullName;
    private String email;
    private List<String> roles;
}