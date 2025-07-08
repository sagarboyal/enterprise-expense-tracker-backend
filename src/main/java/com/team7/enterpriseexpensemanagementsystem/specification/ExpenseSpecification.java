package com.team7.enterpriseexpensemanagementsystem.specification;

import com.team7.enterpriseexpensemanagementsystem.entity.Approval;
import com.team7.enterpriseexpensemanagementsystem.entity.ApprovalLevel;
import com.team7.enterpriseexpensemanagementsystem.entity.ApprovalStatus;
import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ExpenseSpecification {

    public static Specification<Expense> hasStatus(ApprovalStatus status) {
        return (root, query, cb) -> cb.equal(root.get("approvals").get("status"), status == null ? ApprovalStatus.PENDING : status);

    }

    public static Specification<Expense> hasCategory(String categoryName) {
        return (root, query, cb) -> categoryName == null ? cb.conjunction() :
                cb.like(cb.lower(root.get("category").get("name")), "%" + categoryName.toLowerCase() + "%");

    }

    public static Specification<Expense> expenseDateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return cb.conjunction();
            if (start != null && end != null) return cb.between(root.get("expenseDate"), start, end);
            if (start != null) return cb.greaterThanOrEqualTo(root.get("expenseDate"), start);
            return cb.lessThanOrEqualTo(root.get("expenseDate"), end);
        };
    }

    public static Specification<Expense> amountBetween(Double minAmount, Double maxAmount) {
        return (root, query, criteriaBuilder) -> {
          if (minAmount == null && maxAmount == null) return criteriaBuilder.conjunction();
          if (minAmount != null && maxAmount != null) return criteriaBuilder.between(root.get("amount"), minAmount, maxAmount);
          if (minAmount != null) return criteriaBuilder.greaterThan(root.get("amount"), minAmount);
          return criteriaBuilder.lessThan(root.get("amount"), maxAmount);
        };
    }

    public static Specification<Expense> user(Long userId) {
        return ((root, query, criteriaBuilder) -> userId == null ? criteriaBuilder.conjunction() :
                criteriaBuilder.equal(root.get("user").get("id"), userId));
    }

    public static Specification<Expense> hasTitle(String title) {
        return (root, query, cb) ->
                title == null ? cb.conjunction() :
                cb.like(root.get("title"), "%"+title+"%");
    }

    public static Specification<Expense> hasLevel(ApprovalLevel approvalLevel) {
        return (root, query, cb) -> {
            if (approvalLevel == null) return cb.conjunction();
            return cb.equal(root.get("approvals").get("level"), approvalLevel);
        };
    }

    public static Specification<Expense> excludeAdminApprovedIfManagerLevel(String status, String level) {
        return (root, query, cb) -> {
            if (!"APPROVED".equalsIgnoreCase(status) || !"MANAGER".equalsIgnoreCase(level)) {
                return cb.conjunction();
            }

            Subquery<Long> maxIdSubquery = query.subquery(Long.class);
            Root<Approval> maxRoot = maxIdSubquery.from(Approval.class);
            Path<Long> approvalIdPath = maxRoot.get("id");
            maxIdSubquery.select(cb.greatest(approvalIdPath))
                    .where(cb.equal(maxRoot.get("expense").get("id"), root.get("id")));

            Subquery<String> levelSubquery = query.subquery(String.class);
            Root<Approval> levelRoot = levelSubquery.from(Approval.class);
            levelSubquery.select(levelRoot.get("level"))
                    .where(cb.equal(levelRoot.get("id"), maxIdSubquery));

            return cb.notEqual(levelSubquery, ApprovalLevel.ADMIN.toString());
        };
    }


}
