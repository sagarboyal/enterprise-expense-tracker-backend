package com.team7.enterpriseexpensemanagementsystem.specification;

import com.team7.enterpriseexpensemanagementsystem.entity.ContactRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ContactRequestSpecification {
    public static Specification<ContactRequest> hasEmail(String email) {
        return (root, query, criteriaBuilder) ->
                email == null || email.trim().isEmpty() ?
                criteriaBuilder.conjunction() : criteriaBuilder.like(root.get("email"), "%" + email + "%");
    }
    public static Specification<ContactRequest> hasFullName(String fullName) {
        return (root, query, criteriaBuilder) ->
                fullName == null || fullName.trim().isEmpty() ?
                criteriaBuilder.conjunction() : criteriaBuilder.like(root.get("fullName"), "%" + fullName + "%");
    }
    public static Specification<ContactRequest> requestDateBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return cb.conjunction();
            if (start != null && end != null) return cb.between(root.get("timestamp"), start, end);
            if (start != null) return cb.greaterThanOrEqualTo(root.get("timestamp"), start);
            return cb.lessThanOrEqualTo(root.get("timestamp"), end);
        };
    }
}
