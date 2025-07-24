package com.team7.enterpriseexpensemanagementsystem.dto;
import com.team7.enterpriseexpensemanagementsystem.payload.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceDTO {
    private Long id;

    private String invoiceNumber;
    private LocalDateTime generatedAt;
    private Double totalAmount;

    private UserResponse user;
    private String status;

    private String invoiceCloudId;
    private String invoiceUrl;
}
