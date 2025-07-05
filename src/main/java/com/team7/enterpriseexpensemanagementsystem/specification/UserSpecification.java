package com.team7.enterpriseexpensemanagementsystem.specification;

import com.team7.enterpriseexpensemanagementsystem.entity.Expense;
import com.team7.enterpriseexpensemanagementsystem.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User> hasName(String name) {
        return (root, query, criteriaBuilder) -> name == null ?
                criteriaBuilder.conjunction() :
                criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), "%" + name.toLowerCase() + "%");
    }
    public static Specification<User> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> email == null ?
                criteriaBuilder.conjunction() :
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }
    public static Specification<User> hasRole(String role) {
        return (root, query, criteriaBuilder) -> role == null ?
                criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("roles").get("roleName"), role.toUpperCase());
    }
    public static Specification<User> hasMinTotalExpense(Double minAmount) {
        return (root, query, criteriaBuilder) -> {
            if (minAmount == null) return criteriaBuilder.conjunction();

            Join<User, Expense> expenseJoin = root.join("expenses", JoinType.LEFT);
            assert query != null;
            query.groupBy(root.get("id"));
            query.having(criteriaBuilder.greaterThanOrEqualTo(criteriaBuilder.sum(expenseJoin.get("amount")), minAmount));
            return criteriaBuilder.conjunction(); // return something; actual filtering is in groupBy/having
        };
    }

}
