package com.iot.platform.audit;

import com.iot.platform.model.AuditLog;
import com.iot.platform.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(UUID tenantId, UUID userId, String action, String resource, String detail) {
        String dayBucket = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
        AuditLog entry = new AuditLog(tenantId, dayBucket, userId, action, resource, detail);
        auditLogRepository.save(entry);
    }

    public List<AuditLog> getAuditLogs(UUID tenantId, String date, int limit) {
        return auditLogRepository.findByTenantIdAndDay(tenantId, date, limit);
    }
}
