package com.team7.enterpriseexpensemanagementsystem.specification;

import com.team7.enterpriseexpensemanagementsystem.entity.AuditLog;
import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class AuditLogSpecification {
    public static Specification<AuditLog> hasId(Long id) {
        return (root, query, criteriaBuilder) -> id == null ?
                criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("id"), id);
    }

    public static Specification<AuditLog> hasEntityName(String name) {
        return (root, query, criteriaBuilder) -> name == null ||  name.isEmpty() ?
                criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("entityName"), name);
    }

    public static Specification<AuditLog> hasEntityId(Long entityId) {
        return (root, query, criteriaBuilder) -> entityId == null ?
                criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("entityId"), entityId);
    }
    public static Specification<AuditLog> hasDeviceIp(String deviceIp) {
        return (root, query, criteriaBuilder) ->  deviceIp == null || deviceIp.isEmpty() ?
                criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("deviceIp"), deviceIp);
    }

    public static Specification<AuditLog> hasAction(String action) {
        return (root, query, criteriaBuilder) ->  action == null || action.isEmpty() ?
                criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("action"), action.toUpperCase());
    }

    public static Specification<AuditLog> CreatedBy(String createdBy) {
        return (root, query, criteriaBuilder) ->  createdBy == null ||  createdBy.isEmpty() ?
                criteriaBuilder.conjunction() : criteriaBuilder.like(root.get("performedBy"), '%' + createdBy +'%');
    }

    public static Specification<AuditLog> expenseDateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return cb.conjunction();
            if (start != null && end != null) return cb.between(root.get("timestamp"), start, end);
            if (start != null) return cb.greaterThanOrEqualTo(root.get("timestamp"), start);
            return cb.lessThanOrEqualTo(root.get("timestamp"), end);
        };
    }
}
