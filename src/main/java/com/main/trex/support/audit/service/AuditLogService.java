package com.main.trex.support.audit.service;

import com.main.trex.support.audit.entity.AuditLog;
import com.main.trex.shared.payload.response.PagedResponse;

import java.time.LocalDate;

public interface AuditLogService {
    void log(AuditLog auditLog);
    PagedResponse<AuditLog> findAll(Long auditId, String entityName, Long entityId, String deviceIp, String action, String performedBy,
                                    LocalDate startDate, LocalDate endDate, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}


