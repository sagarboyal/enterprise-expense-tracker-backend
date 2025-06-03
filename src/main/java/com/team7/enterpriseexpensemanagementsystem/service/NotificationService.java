package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.entity.Notification;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;

public interface NotificationService {
    Notification saveNotification(Notification notification, Long userId);
    PagedResponse<Notification> getNotifications(Long id, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}
