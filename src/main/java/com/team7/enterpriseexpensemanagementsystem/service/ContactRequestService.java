package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.entity.ContactRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;

import java.time.LocalDateTime;


public interface ContactRequestService {
    ContactRequest saveRequest(ContactRequest contactRequest);
    PagedResponse<ContactRequest> findAllRequest(String email, String fullName, LocalDateTime startDate, LocalDateTime endDate,
                                                 Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    void deleteRequest(Long id);
}
