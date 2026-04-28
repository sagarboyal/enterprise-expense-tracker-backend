package com.main.trex.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.main.trex.organization.entity.Organization;
import com.main.trex.organization.entity.OrganizationInvite;
import com.main.trex.organization.entity.OrganizationMember;
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
public class BusinessUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @OneToMany(mappedBy = "createdBy")
    private List<Organization> organizationsCreated = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
