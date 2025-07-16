package com.team7.enterpriseexpensemanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "invoices")
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber;
    private LocalDateTime generatedAt;
    private Double totalAmount;

    @ManyToOne
    private User user;

    @JsonIgnore
    @OneToMany
    @JoinTable(
            name = "invoice_expenses",
            joinColumns = @JoinColumn(name = "invoice_id"),
            inverseJoinColumns = @JoinColumn(name = "expense_id")
    )
    private List<Expense> expenses = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

}
