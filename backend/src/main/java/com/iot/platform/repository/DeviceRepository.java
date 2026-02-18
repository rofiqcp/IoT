package com.iot.platform.repository;

import com.iot.platform.model.Device;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceRepository extends CassandraRepository<Device, UUID> {

    @Query("SELECT * FROM devices WHERE tenant_id = ?0 AND site_id = ?1")
    List<Device> findByTenantIdAndSiteId(UUID tenantId, UUID siteId);

    @Query("SELECT * FROM devices WHERE tenant_id = ?0 AND site_id = ?1 AND device_id = ?2")
    Device findByTenantIdAndSiteIdAndDeviceId(UUID tenantId, UUID siteId, UUID deviceId);
}
