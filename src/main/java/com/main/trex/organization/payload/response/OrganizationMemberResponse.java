package com.main.trex.organization.payload.response;

import com.main.trex.organization.entity.OrganizationMemberStatus;
import com.main.trex.organization.entity.OrganizationRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrganizationMemberResponse {
    private Long userId;
    private String fullName;
    private String email;
    private OrganizationRole role;
    private OrganizationMemberStatus status;
    private LocalDateTime joinedAt;
}
