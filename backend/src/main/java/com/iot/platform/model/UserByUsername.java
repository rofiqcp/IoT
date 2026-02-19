package com.iot.platform.model;

import org.springframework.data.cassandra.core.mapping.*;

import java.util.UUID;

@Table("users_by_username")
public class UserByUsername {

    @PrimaryKey
    private String username;

    @Column("user_id")
    private UUID userId;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("password_hash")
    private String passwordHash;

    @Column("role")
    private String role;

    public UserByUsername() {}

    public UserByUsername(String username, UUID userId, UUID tenantId,
                         String passwordHash, String role) {
        this.username = username;
        this.userId = userId;
        this.tenantId = tenantId;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
