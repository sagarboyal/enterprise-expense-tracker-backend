package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.entity.Invoice;
public interface EmailService {
    void sendPasswordResetEmail(String email, String resetUrl);
    void sendInvoiceEmail(Invoice invoice, String email);
}
