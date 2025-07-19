package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.config.AppConstants;
import com.team7.enterpriseexpensemanagementsystem.entity.AuditLog;
import com.team7.enterpriseexpensemanagementsystem.entity.Invoice;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.service.AuditLogService;
import com.team7.enterpriseexpensemanagementsystem.service.InvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AuditLogService auditLogService;
    private final InvoiceService invoiceService;
    @Value("${frontend.url}")
    private String frontEndUrl;

    @GetMapping("/audit-log")
    public ResponseEntity<PagedResponse<AuditLog>> getAuditLogs(
            @RequestParam(name = "auditId", required = false) Long auditId,
            @RequestParam(name = "entityName", required = false) String entityName,
            @RequestParam(name = "entityId", required = false) Long entityId,
            @RequestParam(name = "deviceIp", required = false) String deviceIp,
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "performedBy", required = false) String performedBy,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_EXPENSES, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ){
        return ResponseEntity.ok(auditLogService.findAll(auditId, entityName, entityId, deviceIp, action, performedBy,
                startDate, endDate, pageNumber, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/users/invoice")
    public ResponseEntity<PagedResponse<Invoice>> getAllInvoices(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "email",  required = false) String email,
            @RequestParam(name = "invoiceNumber",  required = false) String invoiceNumber,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_INVOICE, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder

    ) {
        return ResponseEntity.ok(invoiceService
                .findAllInvoices(userId, email, invoiceNumber, pageNumber, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/invoice/mail/{userId}")
    public ResponseEntity<String> sendInvoice(@PathVariable Long userId, HttpServletResponse response) {
        invoiceService.sendInvoice(userId, response);
        return ResponseEntity.ok("Mail Sent!");
    }

    @GetMapping("/invoice/expenses/{invoiceId}")
    public ResponseEntity<List<ExpenseResponse>> handleInvoiceExpenses(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(invoiceService.getExpenseList(invoiceId));
    }

    @GetMapping("/invoice/generate/{userId}")
    public ResponseEntity<Void> generateInvoice(@PathVariable Long userId) {
        invoiceService.generateInvoice(userId);
        return ResponseEntity.notFound().build();
    }

    @CrossOrigin(origins = "${frontend.url}")
    @GetMapping("/users/invoice/{invoiceId}/download")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long invoiceId) {
        byte[] pdfBytes = invoiceService.exportInvoiceById(invoiceId);
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "invoice-" + invoice.getInvoiceNumber() + "-" + timestamp + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

}
