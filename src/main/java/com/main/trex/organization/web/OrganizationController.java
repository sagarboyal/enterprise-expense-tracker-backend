package com.main.trex.organization.web;

import com.main.trex.identity.util.AuthUtils;
import com.main.trex.organization.payload.request.CreateOrganizationRequest;
import com.main.trex.organization.payload.request.OrganizationInviteRequest;
import com.main.trex.organization.payload.response.OrganizationInviteResponse;
import com.main.trex.organization.payload.response.OrganizationMemberResponse;
import com.main.trex.organization.payload.response.OrganizationResponse;
import com.main.trex.organization.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        return new ResponseEntity<>(
                organizationService.registerOrganization(request, authUtils.loggedInEmail()),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getMyOrganizations() {
        return ResponseEntity.ok(organizationService.getMyOrganizations(authUtils.loggedInEmail()));
    }

    @PostMapping("/{organizationId}/invites")
    public ResponseEntity<OrganizationInviteResponse> inviteMember(
            @PathVariable Long organizationId,
            @Valid @RequestBody OrganizationInviteRequest request
    ) {
        return new ResponseEntity<>(
                organizationService.inviteMember(organizationId, request, authUtils.loggedInEmail()),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/invites/accept")
    public ResponseEntity<OrganizationInviteResponse> acceptInvite(@RequestParam String token) {
        return ResponseEntity.ok(organizationService.acceptInvite(token, authUtils.loggedInEmail()));
    }

    @GetMapping("/{organizationId}/members")
    public ResponseEntity<List<OrganizationMemberResponse>> getMembers(@PathVariable Long organizationId) {
        return ResponseEntity.ok(organizationService.getMembers(organizationId, authUtils.loggedInEmail()));
    }
}
