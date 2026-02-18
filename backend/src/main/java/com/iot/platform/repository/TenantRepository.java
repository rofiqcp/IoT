package com.iot.platform.repository;

import com.iot.platform.model.Tenant;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TenantRepository extends CassandraRepository<Tenant, UUID> {
}
