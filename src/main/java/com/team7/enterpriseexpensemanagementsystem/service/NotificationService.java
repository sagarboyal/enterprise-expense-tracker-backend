package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.entity.Notification;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;

public interface NotificationService {
    Notification saveNotification(Notification notification, Long userId);
    Notification updateNotificationStatus(Long id);
    void deleteNotification(Long id);
    PagedResponse<Notification> getNotifications(Long userId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}
