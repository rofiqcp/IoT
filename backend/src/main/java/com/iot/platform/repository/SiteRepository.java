package com.iot.platform.repository;

import com.iot.platform.model.Site;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SiteRepository extends CassandraRepository<Site, UUID> {

    List<Site> findByTenantId(UUID tenantId);
}
