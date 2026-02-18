package com.iot.platform.repository;

import com.iot.platform.model.UserByUsername;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserByUsernameRepository extends CassandraRepository<UserByUsername, String> {
}
