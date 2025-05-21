package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.config.AppConstants;
import com.team7.enterpriseexpensemanagementsystem.dto.ExpenseDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.request.ApprovalRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.ExpenseUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpensePagedResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.ExpenseResponse;
import com.team7.enterpriseexpensemanagementsystem.service.ExpenseService;
import com.team7.enterpriseexpensemanagementsystem.utils.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<ExpensePagedResponse> getAllExpenses(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_EXPENSES, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        return ResponseEntity.ok(expenseService.getAllExpenses(pageNumber, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<ExpensePagedResponse> getExpensesByCategoryName(
            @PathVariable String categoryName,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_EXPENSES, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        return ResponseEntity.ok(expenseService.getExpensesByCategoryName(categoryName, pageNumber, pageSize, sortBy, sortOrder));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(@Valid @RequestBody ExpenseDTO expenseDTO) {
        ExpenseResponse created = expenseService.addExpense(expenseDTO, authUtil.loggedInEmail());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ExpenseResponse> updateExpense(@Valid @RequestBody ExpenseUpdateRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(request));
    }

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
}

