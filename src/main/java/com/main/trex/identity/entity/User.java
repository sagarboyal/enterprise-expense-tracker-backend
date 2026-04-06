package com.main.trex.identity.entity;

import com.main.trex.expense.entity.Expense;
import com.main.trex.notification.entity.Notification;
import com.main.trex.organization.entity.Organization;
import com.main.trex.organization.entity.OrganizationInvite;
import com.main.trex.organization.entity.OrganizationMember;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String email;
    private String password;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            mappedBy = "user",
            orphanRemoval = true
    )
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            mappedBy = "user",
            orphanRemoval = true
    )
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private List<OrganizationMember> organizationMemberships = new ArrayList<>();

    @OneToMany(mappedBy = "invitedBy", orphanRemoval = true)
    private List<OrganizationInvite> organizationInvitesSent = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", orphanRemoval = true)
    private List<Organization> organizationsCreated = new ArrayList<>();

    public User(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }
}


