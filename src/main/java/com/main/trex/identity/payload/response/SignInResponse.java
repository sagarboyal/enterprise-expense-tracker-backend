package com.main.trex.identity.payload.response;

import com.main.trex.identity.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignInResponse {
    private String token;
    private UserType activeContext;
}