package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.entity.Invoice;
import jakarta.servlet.http.HttpServletResponse;

public interface EmailService {
    void sendPasswordResetEmail(String email, String resetUrl);
    void sendInvoiceEmail(Invoice invoice, byte[] byteArray);
}
