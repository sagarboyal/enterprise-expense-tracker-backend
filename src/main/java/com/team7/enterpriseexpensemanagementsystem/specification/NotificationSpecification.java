package com.team7.enterpriseexpensemanagementsystem.specification;

import com.team7.enterpriseexpensemanagementsystem.entity.Notification;
import org.springframework.data.jpa.domain.Specification;

public class NotificationSpecification {
    public static Specification<Notification> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) ->
            userId == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("userId"), userId);

    }
}
