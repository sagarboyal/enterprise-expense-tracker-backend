package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.config.AppConstants;
import com.team7.enterpriseexpensemanagementsystem.dto.InvoiceDTO;
import com.team7.enterpriseexpensemanagementsystem.entity.AuditLog;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.service.AuditLogService;
import com.team7.enterpriseexpensemanagementsystem.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AuditLogService auditLogService;
    private final InvoiceService invoiceService;

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
    public ResponseEntity<PagedResponse<InvoiceDTO>> getAllInvoices(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "email",  required = false) String email,
            @RequestParam(name = "invoiceNumber",  required = false) String invoiceNumber,
            @RequestParam(name = "status",  required = false) String status,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_INVOICE, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder

    ) {
        return ResponseEntity.ok(invoiceService
                .findAllInvoices(userId, email, invoiceNumber,status, pageNumber, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/invoice/mail")
    public ResponseEntity<String> sendInvoice(@RequestParam(required = false) Long userId,
                                              @RequestParam Long invoiceId) {
        invoiceService.sendInvoice(userId, invoiceId);
        return ResponseEntity.ok("Mail Sent!");
    }

    @GetMapping("/invoice/generate/{userId}")
    public ResponseEntity<Void> generateInvoice(@PathVariable Long userId) {
        invoiceService.generateInvoice(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/invoice/delete/{invoiceId}")
    public ResponseEntity<String> deleteInvoice(@PathVariable Long invoiceId) {
        invoiceService.deleteInvoice(invoiceId);
        return ResponseEntity.ok("Invoice Deleted!");
    }

}
