package com.main.trex.organization.payload.request;

import com.main.trex.organization.entity.OrganizationRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrganizationInviteRequest {
    @Email(message = "Valid email is required")
    @NotBlank(message = "Invite email is required")
    private String email;

    @NotNull(message = "Organization role is required")
    private OrganizationRole role;
}
