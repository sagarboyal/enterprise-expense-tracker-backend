package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.dto.*;
import com.team7.enterpriseexpensemanagementsystem.service.ExpenseService;
import com.team7.enterpriseexpensemanagementsystem.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AuthUtils authUtils;
    private final ExpenseService expenseService;

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyExpenseDTO>> getMonthlyExpenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(expenseService.getMonthlyAnalytics(authUtils.loggedInUser().getId(), startDate, endDate));
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<WeeklyExpenseDTO>> getMonthlyExpenses() {
        return ResponseEntity.ok(expenseService.getWeeklyAnalytics());
    }

    @GetMapping("/category")
    public ResponseEntity<List<CategoryExpenseDTO>> getCategoryExpenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(expenseService.getCategoryAnalytics(authUtils.loggedInUser().getId(), startDate, endDate));
    }

    @GetMapping("/status")
    public ResponseEntity<List<StatusExpenseDTO>> getStatusExpenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(expenseService.getStatusAnalytics(authUtils.loggedInUser().getId(), startDate, endDate));
    }
    @GetMapping("/summary")
    public ResponseEntity<SummaryDTO> getSummary() {
        SummaryDTO summary = expenseService.getSummary(authUtils.loggedInUser().getId());
        return ResponseEntity.ok(summary);
    }
}
