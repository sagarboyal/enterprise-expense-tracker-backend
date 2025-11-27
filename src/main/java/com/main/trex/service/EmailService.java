package com.main.trex.service;

import com.main.trex.entity.Invoice;
public interface EmailService {
    void sendPasswordResetEmail(String email, String resetUrl);
    void sendInvoiceEmail(Invoice invoice, String email);
}
