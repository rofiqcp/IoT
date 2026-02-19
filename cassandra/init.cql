-- ============================================================
-- IoT Enterprise Platform — Cassandra Schema
-- ============================================================

-- Keyspace (NetworkTopologyStrategy for production)
CREATE KEYSPACE IF NOT EXISTS iot_platform
WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
};

USE iot_platform;

-- ============================================================
-- USERS & AUTH
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       UUID,
    tenant_id     UUID,
    username      TEXT,
    email         TEXT,
    password_hash TEXT,
    role          TEXT,       -- SUPERADMIN, TENANT_ADMIN, OPERATOR, VIEWER
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    PRIMARY KEY (tenant_id, user_id)
);

CREATE TABLE IF NOT EXISTS users_by_username (
    username      TEXT,
    user_id       UUID,
    tenant_id     UUID,
    password_hash TEXT,
    role          TEXT,
    PRIMARY KEY (username)
);

-- ============================================================
-- TENANT HIERARCHY
-- ============================================================
CREATE TABLE IF NOT EXISTS tenants (
    tenant_id   UUID,
    name        TEXT,
    created_at  TIMESTAMP,
    PRIMARY KEY (tenant_id)
);

CREATE TABLE IF NOT EXISTS sites (
    site_id     UUID,
    tenant_id   UUID,
    name        TEXT,
    location    TEXT,
    created_at  TIMESTAMP,
    PRIMARY KEY (tenant_id, site_id)
);

CREATE TABLE IF NOT EXISTS areas (
    area_id     UUID,
    site_id     UUID,
    tenant_id   UUID,
    name        TEXT,
    created_at  TIMESTAMP,
    PRIMARY KEY ((tenant_id, site_id), area_id)
);

CREATE TABLE IF NOT EXISTS lines (
    line_id     UUID,
    area_id     UUID,
    site_id     UUID,
    tenant_id   UUID,
    name        TEXT,
    created_at  TIMESTAMP,
    PRIMARY KEY ((tenant_id, site_id), line_id)
);

-- ============================================================
-- DEVICES
-- ============================================================
CREATE TABLE IF NOT EXISTS devices (
    device_id       UUID,
    tenant_id       UUID,
    site_id         UUID,
    area_id         UUID,
    line_id         UUID,
    name            TEXT,
    device_type     TEXT,
    credential_key  TEXT,
    firmware_version TEXT,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    PRIMARY KEY ((tenant_id, site_id), device_id)
);

-- ============================================================
-- TELEMETRY (time-series, partitioned by device + day bucket)
-- ============================================================
CREATE TABLE IF NOT EXISTS telemetry_by_device (
    tenant_id   UUID,
    device_id   UUID,
    day_bucket  TEXT,         -- format: YYYY-MM-DD
    recorded_at TIMESTAMP,
    metric_name TEXT,
    metric_value DOUBLE,
    unit        TEXT,
    PRIMARY KEY ((tenant_id, device_id, day_bucket), recorded_at, metric_name)
) WITH CLUSTERING ORDER BY (recorded_at DESC)
  AND default_time_to_live = 7776000;  -- 90 days TTL

-- ============================================================
-- DEVICE STATE
-- ============================================================
CREATE TABLE IF NOT EXISTS device_state (
    tenant_id   UUID,
    device_id   UUID,
    online      BOOLEAN,
    last_seen   TIMESTAMP,
    ip_address  TEXT,
    rssi        INT,
    PRIMARY KEY (tenant_id, device_id)
);

-- ============================================================
-- EVENTS
-- ============================================================
CREATE TABLE IF NOT EXISTS events (
    tenant_id   UUID,
    device_id   UUID,
    day_bucket  TEXT,
    occurred_at TIMESTAMP,
    event_type  TEXT,
    severity    TEXT,
    message     TEXT,
    PRIMARY KEY ((tenant_id, device_id, day_bucket), occurred_at)
) WITH CLUSTERING ORDER BY (occurred_at DESC)
  AND default_time_to_live = 7776000;

-- ============================================================
-- ALARMS
-- ============================================================
CREATE TABLE IF NOT EXISTS alarms (
    alarm_id    UUID,
    tenant_id   UUID,
    device_id   UUID,
    rule_name   TEXT,
    severity    TEXT,         -- CRITICAL, WARNING, INFO
    message     TEXT,
    active      BOOLEAN,
    triggered_at TIMESTAMP,
    acknowledged_at TIMESTAMP,
    acknowledged_by UUID,
    PRIMARY KEY ((tenant_id), triggered_at, alarm_id)
) WITH CLUSTERING ORDER BY (triggered_at DESC);

CREATE TABLE IF NOT EXISTS alarm_rules (
    rule_id     UUID,
    tenant_id   UUID,
    metric_name TEXT,
    operator    TEXT,         -- GT, LT, EQ, GTE, LTE
    threshold   DOUBLE,
    severity    TEXT,
    enabled     BOOLEAN,
    created_at  TIMESTAMP,
    PRIMARY KEY (tenant_id, rule_id)
);

-- ============================================================
-- COMMANDS
-- ============================================================
CREATE TABLE IF NOT EXISTS commands (
    command_id  UUID,
    tenant_id   UUID,
    device_id   UUID,
    command_type TEXT,
    payload     TEXT,
    status      TEXT,         -- PENDING, SENT, ACKED, FAILED, TIMEOUT
    created_at  TIMESTAMP,
    acked_at    TIMESTAMP,
    created_by  UUID,
    PRIMARY KEY ((tenant_id, device_id), created_at, command_id)
) WITH CLUSTERING ORDER BY (created_at DESC);

-- ============================================================
-- AUDIT LOG
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_log (
    tenant_id   UUID,
    day_bucket  TEXT,
    logged_at   TIMESTAMP,
    user_id     UUID,
    action      TEXT,
    resource    TEXT,
    detail      TEXT,
    PRIMARY KEY ((tenant_id, day_bucket), logged_at)
) WITH CLUSTERING ORDER BY (logged_at DESC)
  AND default_time_to_live = 15552000;  -- 180 days TTL
