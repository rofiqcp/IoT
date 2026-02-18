package com.iot.platform.repository;

import com.iot.platform.model.AlarmRule;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlarmRuleRepository extends CassandraRepository<AlarmRule, UUID> {

    List<AlarmRule> findByTenantId(UUID tenantId);
}
