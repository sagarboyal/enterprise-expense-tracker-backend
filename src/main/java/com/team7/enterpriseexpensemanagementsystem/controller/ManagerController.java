package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.config.AppConstants;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {
    private final ExpenseService expenseService;

    @GetMapping("/list")
    public ResponseEntity<PagedResponse<ExpenseResponse>> getAllExpenses(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "categoryName", required = false) String categoryName,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "minAmount", required = false) Double minAmount,
            @RequestParam(name = "maxAmount", required = false) Double maxAmount,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_EXPENSES, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        return ResponseEntity.ok(expenseService.getFilteredExpenses(categoryName, "pending", startDate, endDate, minAmount, maxAmount,
                userId,
                pageNumber, pageSize, sortBy, sortOrder));
    }
    @GetMapping("/list/approved")
    public ResponseEntity<PagedResponse<ExpenseResponse>> getApproveExpenses(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "categoryName", required = false) String categoryName,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "minAmount", required = false) Double minAmount,
            @RequestParam(name = "maxAmount", required = false) Double maxAmount,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_EXPENSES, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        return ResponseEntity.ok(expenseService.getFilteredExpenses(categoryName, "manager approve", startDate, endDate, minAmount, maxAmount,
                userId,
                pageNumber, pageSize, sortBy, sortOrder));
    }
    @GetMapping("/list/rejected")
    public ResponseEntity<PagedResponse<ExpenseResponse>> getRejectedExpenses(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "categoryName", required = false) String categoryName,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "minAmount", required = false) Double minAmount,
            @RequestParam(name = "maxAmount", required = false) Double maxAmount,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_EXPENSES, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        return ResponseEntity.ok(expenseService.getFilteredExpenses(categoryName, "manager reject", startDate, endDate, minAmount, maxAmount,
                userId,
                pageNumber, pageSize, sortBy, sortOrder));
    }
}
