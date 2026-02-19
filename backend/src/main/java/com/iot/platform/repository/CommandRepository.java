package com.iot.platform.repository;

import com.iot.platform.model.Command;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommandRepository extends CassandraRepository<Command, UUID> {

    @Query("SELECT * FROM commands WHERE tenant_id = ?0 AND device_id = ?1 LIMIT ?2")
    List<Command> findByTenantIdAndDeviceId(UUID tenantId, UUID deviceId, int limit);
}
