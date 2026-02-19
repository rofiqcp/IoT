package com.iot.platform.repository;

import com.iot.platform.model.User;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends CassandraRepository<User, UUID> {

    List<User> findByTenantId(UUID tenantId);

    @Query("SELECT * FROM users WHERE tenant_id = ?0 AND user_id = ?1")
    User findByTenantIdAndUserId(UUID tenantId, UUID userId);
}
