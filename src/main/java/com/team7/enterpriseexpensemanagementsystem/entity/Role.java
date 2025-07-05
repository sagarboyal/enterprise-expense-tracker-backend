package com.team7.enterpriseexpensemanagementsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int roleId;

    @ToString.Exclude
    @Enumerated(EnumType.STRING)
    private Roles roleName;

    public Role(Roles roleName) {
        this.roleName = roleName;
    }
}
