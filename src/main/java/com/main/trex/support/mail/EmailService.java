package com.main.trex.support.mail;

import com.main.trex.expense.entity.Invoice;
public interface EmailService {
    void sendPasswordResetEmail(String email, String resetUrl);
    void sendInvoiceEmail(Invoice invoice, String email);
}


