package com.team7.enterpriseexpensemanagementsystem.specification;

import com.team7.enterpriseexpensemanagementsystem.entity.Invoice;
import com.team7.enterpriseexpensemanagementsystem.entity.InvoiceStatus;
import org.springframework.data.jpa.domain.Specification;

public class InvoiceSpecification {
    public static Specification<Invoice> byUserId(Long userId) {
        return ((root, query, criteriaBuilder) -> userId == null ?
                criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("user").get("id"), userId)
                );
    }

    public static Specification<Invoice> byUserEmail(String email) {
        return ((root, query, criteriaBuilder) ->
                email == null || email.trim().isEmpty() ?
                criteriaBuilder.conjunction() : criteriaBuilder.like(root.get("user").get("email"), email)
        );
    }

    public static Specification<Invoice> byInvoiceNumber(String invoiceNumber) {
        return ((root, query, criteriaBuilder) ->
                invoiceNumber == null || invoiceNumber.trim().isEmpty() ?
                        criteriaBuilder.conjunction() : criteriaBuilder.like(root.get("invoiceNumber"), invoiceNumber.toUpperCase())
                );
    }

    public static Specification<Invoice> byStatus(String status) {
        return (root, query, criteriaBuilder) ->
                (status == null || status.trim().isEmpty())
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("status"), InvoiceStatus.valueOf(status));
    }

}
