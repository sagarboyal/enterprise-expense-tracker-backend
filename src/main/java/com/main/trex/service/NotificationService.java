package com.main.trex.service;

import com.main.trex.entity.Notification;
import com.main.trex.payload.response.PagedResponse;

public interface NotificationService {
    Notification saveNotification(Notification notification, Long userId);
    Notification updateNotificationStatus(Long id);
    void deleteNotification(Long id);
    PagedResponse<Notification> getNotifications(Long userId, Boolean status, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    long getUnreadCount();
    void markAllAsRead();
}
