package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import com.team7.enterpriseexpensemanagementsystem.entity.Invoice;
import com.team7.enterpriseexpensemanagementsystem.entity.User;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface InvoiceService {
    void generateInvoice(User user, Expense expense);
    void generateInvoice(Long userId);
    Invoice exportInvoice(Long userId, HttpServletResponse response);
    void sendInvoice(Long userId, HttpServletResponse response);
    PagedResponse<Invoice> findAllInvoices(Long id, String email, String invoiceNumber, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    byte[] exportInvoiceById(Long invoiceId);
    void streamInvoicePdfById(Long invoiceId, HttpServletResponse response, boolean inline);
    List<ExpenseResponse> getExpenseList(Long invoiceId);
    Invoice getInvoiceById(Long invoiceId);
}
