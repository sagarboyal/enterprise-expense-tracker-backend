package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.config.AppConstants;
import com.team7.enterpriseexpensemanagementsystem.entity.Invoice;
import com.team7.enterpriseexpensemanagementsystem.entity.User;
import com.team7.enterpriseexpensemanagementsystem.payload.request.RoleUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.UserRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.UserUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.UserResponse;
import com.team7.enterpriseexpensemanagementsystem.service.InvoiceService;
import com.team7.enterpriseexpensemanagementsystem.service.UserService;
import com.team7.enterpriseexpensemanagementsystem.utils.AuthUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthUtils authUtils;
    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(
            @RequestParam(name = "fullName", required = false) String name,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name="role", required = false) String role,
            @RequestParam(name = "minAmount", required = false) Double minAmount,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_EXPENSES, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        PagedResponse<UserResponse> response = userService.getAllUsers(name, email, role, minAmount,
                        pageNumber, pageSize, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUserById() {
        return ResponseEntity.ok(userService.getUserById(authUtils.loggedInUser().getId()));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }
    @PutMapping
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{id}/roles")
    public ResponseEntity<UserResponse> updateUserRoles(@PathVariable Long id,
                                                        @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(userService.updateRoles(id, request));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/invoices")
    public ResponseEntity<PagedResponse<Invoice>>  getAllInvoices(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_INVOICE, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ){
        return ResponseEntity.ok(invoiceService.findAllInvoices(userId, pageNumber, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/user-invoices")
    public ResponseEntity<PagedResponse<Invoice>>  getAllUserInvoices(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_INVOICE, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ){
        return ResponseEntity.ok(invoiceService.findAllInvoices(authUtils.loggedInUser().getId(), pageNumber, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/invoices/{invoiceId}/export")
    public ResponseEntity<Void> downloadInvoiceById(@PathVariable Long invoiceId, HttpServletResponse response) {
        invoiceService.exportInvoiceById(invoiceId, response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/invoices/{invoiceId}/view")
    public void viewInvoice(@PathVariable Long invoiceId, HttpServletResponse response) {
        invoiceService.streamInvoicePdfById(invoiceId, response, true);
    }

}
