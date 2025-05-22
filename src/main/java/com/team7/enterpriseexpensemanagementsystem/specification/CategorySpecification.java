package com.team7.enterpriseexpensemanagementsystem.specification;

import com.team7.enterpriseexpensemanagementsystem.entity.Category;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecification {
    public static Specification<Category> hasName(String name) {
        return (root, criteriaQuery, criteriaBuilder) ->
                name == null ? null :
                criteriaBuilder.equal(root.get("name"), name);
    }
    public static Specification<Category> hasId(Long id) {
        return (root, criteriaQuery, criteriaBuilder) ->
                id != null ? criteriaBuilder.equal(root.get("id"), id) : null;
    }
}
