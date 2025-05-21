package com.team7.enterpriseexpensemanagementsystem.entity;

import lombok.Getter;

@Getter
public enum Approval {
    PENDING("Waiting for manager approval."),
    APPROVED_BY_MANAGER("Approved by manager. Awaiting admin approval."),
    APPROVED_BY_ADMIN("Expense fully approved."),
    REJECTED("Expense was rejected.");

    private final String message;

    Approval(String message) {
        this.message = message;
    }
}
