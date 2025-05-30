package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.entity.AuditLog;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;

public interface AuditLogService {
    void log(AuditLog auditLog);
    PagedResponse<AuditLog> findAll();
}
