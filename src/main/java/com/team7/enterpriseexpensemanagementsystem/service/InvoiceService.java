package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import com.team7.enterpriseexpensemanagementsystem.entity.Invoice;
import com.team7.enterpriseexpensemanagementsystem.entity.User;
import jakarta.servlet.http.HttpServletResponse;

public interface InvoiceService {
    void generateInvoice(User user, Expense expense);
    Invoice exportInvoice(Long userId, HttpServletResponse response);
    void sendInvoice(Long userId, HttpServletResponse response);
}
