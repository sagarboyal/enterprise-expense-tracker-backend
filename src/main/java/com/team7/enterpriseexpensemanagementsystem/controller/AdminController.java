package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.config.AppConstants;
import com.team7.enterpriseexpensemanagementsystem.entity.AuditLog;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.service.AuditLogService;
import com.team7.enterpriseexpensemanagementsystem.service.ExpenseService;
import jakarta.servlet.http.HttpServletResponse;
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
}
