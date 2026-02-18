package com.iot.platform.repository;

import com.iot.platform.model.AuditLog;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends CassandraRepository<AuditLog, UUID> {

    @Query("SELECT * FROM audit_log WHERE tenant_id = ?0 AND day_bucket = ?1 LIMIT ?2")
    List<AuditLog> findByTenantIdAndDay(UUID tenantId, String dayBucket, int limit);
}
