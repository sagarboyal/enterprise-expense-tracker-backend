package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.entity.Notification;
import com.team7.enterpriseexpensemanagementsystem.entity.User;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceNotFoundException;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.repository.NotificationRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.UserRepository;
import com.team7.enterpriseexpensemanagementsystem.service.NotificationService;
import com.team7.enterpriseexpensemanagementsystem.specification.NotificationSpecification;
import com.team7.enterpriseexpensemanagementsystem.utils.AuthUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AuthUtils authUtils;

    @Override
    public Notification saveNotification(Notification notification, Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
        notification.setUser(user);
        notification.setSentAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Override
    public Notification updateNotificationStatus(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification with id " + id + " not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification with id " + id + " not found"));
        notificationRepository.delete(notification);
    }

    @Override
    public PagedResponse<Notification> getNotifications(Long userId, Boolean status, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Specification<Notification> specs = Specification.where(NotificationSpecification.hasUserId(userId))
                .and(NotificationSpecification.isRead(status));

        Sort sort = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Notification> pageResponse = notificationRepository.findAll(specs, pageable);
        List<Notification> notificationList = pageResponse.getContent();

        return PagedResponse.<Notification>builder()
                .content(notificationList)
                .pageNumber(pageResponse.getNumber())
                .pageSize(pageResponse.getSize())
                .totalElements(pageResponse.getTotalElements())
                .totalPages(pageResponse.getTotalPages())
                .lastPage(pageResponse.isLast())
                .build();
    }

    @Override
    public long getUnreadCount() {
        return notificationRepository.countByReadFalseAndUser_Id(authUtils.loggedInUser().getId());
    }

    @Transactional
    @Override
    public void markAllAsRead() {
        notificationRepository.markAllAsReadByUserId(authUtils.loggedInUser().getId());
    }
}
