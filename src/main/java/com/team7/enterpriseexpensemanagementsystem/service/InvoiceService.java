package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import com.team7.enterpriseexpensemanagementsystem.entity.Invoice;
import com.team7.enterpriseexpensemanagementsystem.entity.User;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;

import java.util.List;

public interface InvoiceService {
    void generateInvoice(User user, Expense expense);
    void generateInvoice(Long userId);
    void deleteInvoice(Long invoiceId);
    Invoice regenerateInvoice(Long invoiceId);
    void sendInvoice(Long userId, Long invoiceId);
    PagedResponse<Invoice> findAllInvoices(Long id, String email, String invoiceNumber,
                                           Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    List<ExpenseResponse> getExpenseList(Long invoiceId);
    Invoice getInvoiceById(Long invoiceId);
}
