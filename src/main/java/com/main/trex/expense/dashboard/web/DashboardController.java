package com.main.trex.expense.dashboard.web;

import com.main.trex.expense.dashboard.service.DashboardService;
import com.main.trex.expense.dto.CategoryExpenseDTO;
import com.main.trex.expense.dto.MonthlyExpenseDTO;
import com.main.trex.expense.dto.StatusExpenseDTO;
import com.main.trex.expense.dto.SummaryDTO;
import com.main.trex.expense.dto.WeeklyExpenseDTO;
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
@RequestMapping({"/api/dashboard", "/api/analytics"})
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyExpenseDTO>> getMonthlyExpenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(dashboardService.getMonthlyAnalytics(startDate, endDate));
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<WeeklyExpenseDTO>> getWeeklyExpenses() {
        return ResponseEntity.ok(dashboardService.getWeeklyAnalytics());
    }

    @GetMapping("/category")
    public ResponseEntity<List<CategoryExpenseDTO>> getCategoryExpenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(dashboardService.getCategoryAnalytics(startDate, endDate));
    }

    @GetMapping("/status")
    public ResponseEntity<List<StatusExpenseDTO>> getStatusExpenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(dashboardService.getStatusAnalytics(startDate, endDate));
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryDTO> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }
}
