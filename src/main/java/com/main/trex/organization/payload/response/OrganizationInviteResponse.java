package com.main.trex.organization.payload.response;

import com.main.trex.organization.entity.OrganizationInviteStatus;
import com.main.trex.organization.entity.OrganizationRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrganizationInviteResponse {
    private Long inviteId;
    private Long organizationId;
    private String organizationName;
    private String invitedEmail;
    private OrganizationRole role;
    private String token;
    private LocalDateTime expiresAt;
    private OrganizationInviteStatus status;
}
