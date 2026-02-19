package com.iot.platform.repository;

import com.iot.platform.model.TelemetryRecord;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TelemetryRepository extends CassandraRepository<TelemetryRecord, UUID> {

    @Query("SELECT * FROM telemetry_by_device WHERE tenant_id = ?0 AND device_id = ?1 AND day_bucket = ?2 LIMIT ?3")
    List<TelemetryRecord> findByDeviceAndDay(UUID tenantId, UUID deviceId, String dayBucket, int limit);
}
