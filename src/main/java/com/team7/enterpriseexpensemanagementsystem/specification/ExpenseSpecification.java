package com.team7.enterpriseexpensemanagementsystem.specification;

import com.team7.enterpriseexpensemanagementsystem.entity.ApprovalStatus;
import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ExpenseSpecification {

    public static Specification<Expense> hasStatus(ApprovalStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };

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
}
