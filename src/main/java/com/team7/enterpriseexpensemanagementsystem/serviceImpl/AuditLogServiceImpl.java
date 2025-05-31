package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.entity.AuditLog;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.repository.AuditLogRepository;
import com.team7.enterpriseexpensemanagementsystem.service.AuditLogService;
import com.team7.enterpriseexpensemanagementsystem.specification.AuditLogSpecification;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;

    @Override
    public void log(AuditLog auditLog) {
        auditLog.setDeviceIp(getClientIp());
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    @Override
    public PagedResponse<AuditLog> findAll(Long auditId, String entityName, Long entityId, String deviceIp, String action, String performedBy,
                                           LocalDate startDate, LocalDate endDate, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Specification<AuditLog> specs = Specification.where(AuditLogSpecification.hasId(auditId))
                .and(AuditLogSpecification.hasEntityName(entityName))
                .and(AuditLogSpecification.hasEntityId(entityId))
                .and(AuditLogSpecification.hasDeviceIp(deviceIp))
                .and(AuditLogSpecification.hasAction(action))
                .and(AuditLogSpecification.CreatedBy(performedBy))
                .and(AuditLogSpecification.expenseDateBetween(startDate, endDate));

        Sort sort = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable page = PageRequest.of(pageNumber, pageSize, sort);
        Page<AuditLog> pageResponse = auditLogRepository.findAll(specs, page);
        List<AuditLog> auditLogs = pageResponse.getContent();

        return PagedResponse.<AuditLog>builder()
                .content(auditLogs)
                .pageNumber(pageResponse.getNumber())
                .pageSize(pageResponse.getSize())
                .totalElements(pageResponse.getTotalElements())
                .totalPages(pageResponse.getTotalPages())
                .lastPage(pageResponse.isLast())
                .build();
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            ip = ip.split(",")[0].trim(); // first IP in the chain
        } else {
            ip = request.getRemoteAddr();
        }

        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }
}
