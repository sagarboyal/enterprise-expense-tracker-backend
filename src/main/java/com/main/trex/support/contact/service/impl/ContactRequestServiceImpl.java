package com.main.trex.support.contact.service.impl;

import com.main.trex.support.contact.entity.ContactRequest;
import com.main.trex.shared.exception.ApiException;
import com.main.trex.shared.payload.response.PagedResponse;
import com.main.trex.support.contact.repository.ContactRequestRepository;
import com.main.trex.support.contact.service.ContactRequestService;
import com.main.trex.support.contact.specification.ContactRequestSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactRequestServiceImpl implements ContactRequestService {
    private final ContactRequestRepository contactRequestRepository;

    @Override
    public ContactRequest saveRequest(ContactRequest contactRequest) {
        contactRequest.setTimestamp(LocalDateTime.now());
        contactRequest.setId(null);
        return contactRequestRepository.save(contactRequest);
    }

    @Override
    public PagedResponse<ContactRequest> findAllRequest(String email, String fullName, LocalDateTime startDate, LocalDateTime endDate,
                                                        Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Specification<ContactRequest> specs = Specification.where(ContactRequestSpecification.hasEmail(email))
                .and(ContactRequestSpecification.hasFullName(fullName))
                .and(ContactRequestSpecification.requestDateBetween(startDate, endDate));

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
        Page<ContactRequest> contactRequestPage = contactRequestRepository.findAll(specs, pageDetails);
        List<ContactRequest> contactRequestList = contactRequestPage.getContent();

        return PagedResponse.<ContactRequest>builder()
                .content(contactRequestList)
                .pageNumber(contactRequestPage.getNumber())
                .pageSize(contactRequestPage.getSize())
                .totalElements(contactRequestPage.getTotalElements())
                .totalPages(contactRequestPage.getTotalPages())
                .lastPage(contactRequestPage.isLast())
                .build();
    }

    @Override
    public void deleteRequest(Long id) {
        ContactRequest request = contactRequestRepository.findById(id)
                .orElseThrow(() -> new ApiException("request id not found"));
        contactRequestRepository.delete(request);
    }
}


