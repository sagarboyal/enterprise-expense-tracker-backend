package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.config.AppConstants;
import com.team7.enterpriseexpensemanagementsystem.dto.ExpenseDTO;
import com.team7.enterpriseexpensemanagementsystem.entity.Approval;
import com.team7.enterpriseexpensemanagementsystem.payload.request.ApprovalRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.ExpenseUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.service.ExpenseService;
import com.team7.enterpriseexpensemanagementsystem.utils.AuthUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final AuthUtils authUtil;

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseByExpenseId(id));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ExpenseResponse>> getAllExpenses(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "categoryName", required = false) String categoryName,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "level", required = false) String level,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "minAmount", required = false) Double minAmount,
            @RequestParam(name = "maxAmount", required = false) Double maxAmount,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_EXPENSES, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
            @RequestParam(name="export", required = false, defaultValue = "false") Boolean export,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(expenseService.getFilteredExpenses(title, categoryName, status, level, startDate, endDate, minAmount, maxAmount,
                authUtil.loggedInUser().getId(),
                pageNumber, pageSize, sortBy, sortOrder, export, response));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/request-list")
    public ResponseEntity<PagedResponse<ExpenseResponse>> getAllExpenses(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "categoryName", required = false) String categoryName,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "level", required = false) String level,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "minAmount", required = false) Double minAmount,
            @RequestParam(name = "maxAmount", required = false) Double maxAmount,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_EXPENSES, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
            @RequestParam(name="export", required = false, defaultValue = "false") Boolean export,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(expenseService.getFilteredExpenses(title, categoryName, status, level, startDate, endDate, minAmount, maxAmount,
                userId,
                pageNumber, pageSize, sortBy, sortOrder, export, response));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(@Valid @RequestBody ExpenseDTO expenseDTO) {
        ExpenseResponse created = expenseService.addExpense(expenseDTO, authUtil.loggedInEmail());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> createExpenses(@RequestBody List<ExpenseDTO> expenses) {
        List<ExpenseDTO> saved = expenseService.saveAll(expenses);
        return ResponseEntity.ok(saved);
    }


    @PutMapping
    public ResponseEntity<ExpenseResponse> updateExpense(@Valid @RequestBody ExpenseUpdateRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(request));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PutMapping("/approve/{id}")
    public ResponseEntity<ExpenseResponse> approveExpense(@Valid @RequestBody ApprovalRequest request,
                                                          @PathVariable Long id) {
        return ResponseEntity.ok(expenseService.updateExpenseStatus(id,
                                                                    request,
                                                                    authUtil.loggedInEmail()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/approval-stack/{id}")
    public ResponseEntity<List<Approval>> getApprovalStack(@PathVariable("id") Long id) {
        return ResponseEntity.ok(expenseService.getApprovalStack(id));
    }

}

