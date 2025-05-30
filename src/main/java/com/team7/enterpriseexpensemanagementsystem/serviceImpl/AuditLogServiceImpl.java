package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.entity.AuditLog;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.repository.AuditLogRepository;
import com.team7.enterpriseexpensemanagementsystem.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(AuditLog auditLog) {
        auditLog.setDeviceIp(getIp());
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    @Override
    public PagedResponse<AuditLog> findAll() {
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        return PagedResponse.<AuditLog>builder()
                .content(auditLogs)
                .build();
    }

    private String getIp(){
        String ip = "";
        try{
            ip = InetAddress.getLocalHost().getHostAddress();
        }catch (UnknownHostException ex){
            System.out.println(ex.getMessage());;
        }
        return ip;
    }
}
