package com.team7.enterpriseexpensemanagementsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "approval_records")
public class Approval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long userId;

    @Enumerated(EnumType.STRING)
    public ApprovalLevel level;

    @Enumerated(EnumType.STRING)
    public ApprovalStatus status;

    public LocalDateTime actionTime;

    @ManyToOne
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    private String comment;

}
