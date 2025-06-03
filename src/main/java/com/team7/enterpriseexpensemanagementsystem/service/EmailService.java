package com.team7.enterpriseexpensemanagementsystem.service;

public interface EmailService {
    void sendPasswordResetEmail(String email, String resetUrl);
}
