package com.main.trex.identity.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.main.trex.organization.entity.Organization;
import com.main.trex.organization.entity.OrganizationInvite;
import com.main.trex.organization.entity.OrganizationMember;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "business_users")
@Builder
public class BusinessUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference("user-businessProfile")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String employeeCode;

    private String jobTitle;

    private String department;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "businessUser")
    private List<OrganizationMember> organizationMemberships = new ArrayList<>();

    @OneToMany(mappedBy = "invitedBy")
    private List<OrganizationInvite> organizationInvitesSent = new ArrayList<>();

    @OneToOne(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private Organization organization;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}