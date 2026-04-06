package com.main.trex.organization.service;

import com.main.trex.organization.payload.request.CreateOrganizationRequest;
import com.main.trex.organization.payload.request.OrganizationInviteRequest;
import com.main.trex.organization.payload.response.OrganizationInviteResponse;
import com.main.trex.organization.payload.response.OrganizationMemberResponse;
import com.main.trex.organization.payload.response.OrganizationResponse;

import java.util.List;

public interface OrganizationService {
    OrganizationResponse registerOrganization(CreateOrganizationRequest request, String creatorEmail);
    OrganizationInviteResponse inviteMember(Long organizationId, OrganizationInviteRequest request, String inviterEmail);
    OrganizationInviteResponse acceptInvite(String token, String currentUserEmail);
    List<OrganizationMemberResponse> getMembers(Long organizationId, String requesterEmail);
    List<OrganizationResponse> getMyOrganizations(String userEmail);
}
