package com.iot.platform.repository;

import com.iot.platform.model.Alarm;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlarmRepository extends CassandraRepository<Alarm, UUID> {

    @Query("SELECT * FROM alarms WHERE tenant_id = ?0 LIMIT ?1")
    List<Alarm> findByTenantId(UUID tenantId, int limit);
}
