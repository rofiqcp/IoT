package com.iot.platform.repository;

import com.iot.platform.model.DeviceState;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceStateRepository extends CassandraRepository<DeviceState, UUID> {

    @Query("SELECT * FROM device_state WHERE tenant_id = ?0 AND device_id = ?1")
    DeviceState findByTenantIdAndDeviceId(UUID tenantId, UUID deviceId);

    @Query("SELECT * FROM device_state WHERE tenant_id = ?0")
    List<DeviceState> findByTenantId(UUID tenantId);
}
