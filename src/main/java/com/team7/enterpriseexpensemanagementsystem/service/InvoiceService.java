package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import com.team7.enterpriseexpensemanagementsystem.entity.Invoice;
import com.team7.enterpriseexpensemanagementsystem.entity.User;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface InvoiceService {
    void generateInvoice(User user, Expense expense);
    Invoice exportInvoice(Long userId, HttpServletResponse response);
    void sendInvoice(Long userId, HttpServletResponse response);
    PagedResponse<Invoice> findAllInvoices(Long id, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    Invoice exportInvoiceById(Long invoiceId, HttpServletResponse response);

    void streamInvoicePdfById(Long invoiceId, HttpServletResponse response, boolean inline);
}
