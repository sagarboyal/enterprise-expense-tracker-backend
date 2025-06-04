package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.config.AppConstants;
import com.team7.enterpriseexpensemanagementsystem.entity.Notification;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.service.NotificationService;
import com.team7.enterpriseexpensemanagementsystem.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<PagedResponse<Notification>> getNotifications(
            @RequestParam(name= "status", required = false) Boolean status,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_EXPENSES, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder

    ){
        return ResponseEntity.ok(notificationService
                .getNotifications(authUtils.loggedInUser().getId(), status, pageNumber, pageSize, sortBy, sortOrder));
    }

    @PatchMapping("/status/{id}")
    public ResponseEntity<Notification> updateNotificationStatus(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.updateNotificationStatus(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long id){
        notificationService.deleteNotification(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
