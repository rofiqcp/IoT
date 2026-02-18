# IoT Enterprise Platform — System Architecture

## 1. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        DEVICE LAYER                                 │
│   ESP32 (thousands)  ──MQTT/TLS──►  EMQX Cluster (3-node)         │
└───────────────────────────────────────┬─────────────────────────────┘
                                        │
┌───────────────────────────────────────▼─────────────────────────────┐
│                        EDGE / GATEWAY LAYER                         │
│   Python Gateway (asyncio)                                          │
│   • MQTT subscriber  → Kafka producer  (telemetry, state, events)  │
│   • Kafka consumer   → MQTT publisher  (commands)                  │
│   • Disk buffer for offline resilience                              │
└───────────────────────────────────────┬─────────────────────────────┘
                                        │
┌───────────────────────────────────────▼─────────────────────────────┐
│                        STREAMING LAYER                              │
│   Apache Kafka (3-broker cluster)                                   │
│   Topics: telemetry, device-state, events, commands, alarms, audit │
└───────────────────────────────────────┬─────────────────────────────┘
                                        │
┌───────────────────────────────────────▼─────────────────────────────┐
│                        BACKEND LAYER                                │
│   Spring Boot 3.x                                                   │
│   • REST API  • Kafka consumers  • WebSocket  • JWT/RBAC           │
│   • Alarm engine  • Audit logger  • Tenant context                 │
└───────────────────────────────────────┬─────────────────────────────┘
                                        │
┌───────────────────────────────────────▼─────────────────────────────┐
│                        DATABASE LAYER                               │
│   Apache Cassandra (3-node cluster)                                 │
│   • Telemetry (time-series, TTL 90d)                               │
│   • Device state  • Events  • Alarms  • Users  • Audit            │
└───────────────────────────────────────┬─────────────────────────────┘
                                        │
┌───────────────────────────────────────▼─────────────────────────────┐
│                        FRONTEND LAYER                               │
│   React + TypeScript + Vite                                         │
│   • WebSocket realtime  • Recharts  • RBAC UI  • Zustand          │
└─────────────────────────────────────────────────────────────────────┘
```

## 2. Tenant Hierarchy

```
Tenant (company)
 └── Site (factory / building)
      └── Area (zone / floor)
           └── Line (production line)
                └── Device (ESP32 sensor/actuator)
```

## 3. Data Flows

### A. Telemetry Flow
```
ESP32 → MQTT publish (QoS 1)
     → EMQX broker
     → Python Gateway (subscriber)
     → Kafka topic: iot.telemetry
     → Spring Boot consumer
     → Cassandra (telemetry_by_device)
     → WebSocket push to React dashboard
```

### B. Command Flow
```
React UI → REST POST /api/commands
        → Spring Boot → Kafka topic: iot.commands
        → Python Gateway (consumer)
        → MQTT publish to device
        → ESP32 executes command
        → ESP32 publishes ACK → MQTT → Gateway → Kafka → Backend
```

### C. Device State (Presence)
```
ESP32 connects with LWT message (offline payload)
ESP32 publishes heartbeat every 30s
Gateway detects state changes → Kafka: iot.device-state
Backend updates Cassandra device_state table
Frontend shows online/offline badge via WebSocket
```

## 4. MQTT Design

### Topic Naming
```
{tenant_id}/{site_id}/{device_id}/telemetry     # device → cloud
{tenant_id}/{site_id}/{device_id}/state          # device → cloud (retained)
{tenant_id}/{site_id}/{device_id}/event          # device → cloud
{tenant_id}/{site_id}/{device_id}/command        # cloud → device
{tenant_id}/{site_id}/{device_id}/command/ack    # device → cloud
{tenant_id}/{site_id}/{device_id}/lwt            # LWT (broker-managed)
```

### QoS Strategy
| Message Type | QoS | Reason |
|---|---|---|
| Telemetry | 1 | At-least-once, tolerate duplicates |
| State | 1 + retained | Must survive reconnect |
| Command | 1 | Ensure delivery |
| Command ACK | 1 | Confirm execution |
| LWT | 1 + retained | Offline detection |

### Authentication
- Username: `{tenant_id}:{device_id}`
- Password: pre-shared key (rotatable via provisioning API)
- Future: X.509 client certificates

## 5. Kafka Design

### Topics
| Topic | Partitions | Retention | Key Format |
|---|---|---|---|
| iot.telemetry | 12 | 7 days | tenant_id\|device_id |
| iot.device-state | 6 | 14 days | tenant_id\|device_id |
| iot.events | 6 | 30 days | tenant_id\|device_id |
| iot.commands | 6 | 3 days | tenant_id\|device_id |
| iot.command-acks | 6 | 3 days | tenant_id\|device_id |
| iot.alarms | 6 | 30 days | tenant_id\|device_id |
| iot.audit | 3 | 90 days | tenant_id\|user_id |

### Consumer Groups
- `backend-telemetry-cg` — processes telemetry
- `backend-state-cg` — processes device state changes
- `backend-events-cg` — processes events
- `backend-commands-cg` — (gateway consumes commands)
- `backend-alarms-cg` — processes alarm triggers
- `gateway-commands-cg` — gateway consuming commands for MQTT publish

## 6. Security Design

### RBAC Roles
| Role | Permissions |
|---|---|
| SUPERADMIN | Full system access, manage tenants |
| TENANT_ADMIN | Manage own tenant: users, sites, devices |
| OPERATOR | Send commands, acknowledge alarms |
| VIEWER | Read-only dashboards |

### JWT Structure
- Access token: 15 min TTL
- Refresh token: 7 day TTL
- Claims: userId, tenantId, roles[]

## 7. Scalability Roadmap

| Phase | Devices | EMQX | Kafka | Cassandra | Backend |
|---|---|---|---|---|---|
| 1 | 100 | 1 node | 1 broker | 1 node | 1 instance |
| 2 | 1,000 | 2 nodes | 3 brokers | 3 nodes | 2 instances |
| 3 | 10,000 | 3 nodes | 3 brokers, 12 partitions | 3 nodes | 3 instances |
| 4 | 100,000 | 5 nodes | 6 brokers, 24 partitions | 6 nodes | 6 instances + HPA |

## 8. Observability

- **Metrics**: Prometheus + Grafana
- **Logging**: Structured JSON → stdout (collected by Fluentd/Loki)
- **Tracing**: Spring Micrometer → Zipkin/Jaeger
- **Health**: `/actuator/health` (backend), custom health checks per service
- **Kafka lag**: Burrow or built-in consumer lag metrics
- **EMQX**: Built-in dashboard + Prometheus plugin

## 9. Deployment

### Development: `docker-compose.yml`
Single command: `docker-compose up -d`

### Production: Kubernetes
- Helm charts per service
- HPA for backend pods
- StatefulSets for Cassandra, Kafka, EMQX
- Ingress with TLS termination
- Secrets via Vault / K8s Secrets
