package com.main.trex.expense.service;

import com.main.trex.expense.dto.InvoiceDTO;
import com.main.trex.expense.entity.Expense;
import com.main.trex.expense.entity.Invoice;
import com.main.trex.identity.entity.User;
import com.main.trex.expense.payload.response.ExpenseResponse;
import com.main.trex.shared.payload.response.PagedResponse;

import java.util.List;

public interface InvoiceService {
    void generateInvoice(User user, Expense expense);
    void generateInvoice(Long userId);
    void deleteInvoice(Long invoiceId);
    Invoice regenerateInvoice(Long invoiceId);
    void sendInvoice(Long userId, Long invoiceId);
    PagedResponse<InvoiceDTO> findAllInvoices(Long id, String email, String invoiceNumber, String status,
                                              Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    List<ExpenseResponse> getExpenseList(Long invoiceId);
    Invoice getInvoiceById(Long invoiceId);
}


