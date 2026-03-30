package com.main.trex.support.contact.service;

import com.main.trex.support.contact.entity.ContactRequest;
import com.main.trex.shared.payload.response.PagedResponse;

import java.time.LocalDateTime;


public interface ContactRequestService {
    ContactRequest saveRequest(ContactRequest contactRequest);
    PagedResponse<ContactRequest> findAllRequest(String email, String fullName, LocalDateTime startDate, LocalDateTime endDate,
                                                 Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    void deleteRequest(Long id);
}


