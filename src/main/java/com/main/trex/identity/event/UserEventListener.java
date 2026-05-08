package com.main.trex.identity.event;

import com.main.trex.identity.entity.User;
import com.main.trex.notification.entity.Notification;
import com.main.trex.notification.service.NotificationService;
import com.main.trex.shared.util.ObjectMapperUtils;
import com.main.trex.support.audit.entity.AuditLog;
import com.main.trex.support.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final ObjectMapperUtils mapperUtils;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserCreated(UserCreatedEvent event) {
        User user = event.user();
        auditLogService.log(AuditLog.builder()
                .entityName("user")
                .entityId(user.getId())
                .action("CREATED")
                .performedBy(user.getEmail())
                .oldValue(null)
                .newValue(mapperUtils.convertToJson(user))
                .build());
        notificationService.saveNotification(
                new Notification(event.notificationMessage()),
                user.getId()
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOAuthUserCreated(OAuthUserCreatedEvent event) {
        User user = event.user();
        auditLogService.log(AuditLog.builder()
                .entityName("user")
                .entityId(user.getId())
                .action("CREATED_OAUTH")
                .performedBy(user.getEmail())
                .oldValue(null)
                .newValue(mapperUtils.convertToJson(user))
                .build());
        notificationService.saveNotification(
                new Notification(event.notificationMessage()),
                user.getId()
        );
    }
}