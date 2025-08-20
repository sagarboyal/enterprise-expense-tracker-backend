package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.config.AppConstants;
import com.team7.enterpriseexpensemanagementsystem.entity.ContactRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.service.ContactRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactRequestController {
    private final ContactRequestService contactRequestService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<PagedResponse<ContactRequest>> getAllRequest(
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "fullName", required = false) String fullName,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_EXPENSES, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
            )
    {
        return ResponseEntity.ok(contactRequestService.findAllRequest(email, fullName, startDate, endDate,
                pageNumber, pageSize, sortBy, sortOrder));
    }

    @PostMapping
    public ResponseEntity<ContactRequest> createContactRequest(@RequestBody ContactRequest contactRequest){
        return ResponseEntity.ok(contactRequestService.saveRequest(contactRequest));
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContactRequest(@PathVariable Long id){
        contactRequestService.deleteRequest(id);
        return ResponseEntity.ok().body("Deleted request with id: " + id);
    }
}
