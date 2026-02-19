# IoT Enterprise Platform

Multi-tenant IoT platform supporting **ESP32** and **Python Mini PC** devices.
Both device types connect directly to the central EMQX cluster — there is no
edge gateway layer.

## Architecture Overview

```
ESP32 devices ─────────┐
                        ├──► EMQX Cluster ──► Bridge ──► Kafka ──► Spring Boot ──► Cassandra
Python Mini PC ────────┘         ▲                                       │
devices                          │                                       ▼
                           (commands)                              React Frontend
```

**Tenant hierarchy:** Tenant → Site → Area → Line → Device

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the full design.

---

## Repository Structure

```
.
├── backend/           # Spring Boot 3.x backend (REST, Kafka, WebSocket, JWT)
│   ├── src/main/java/com/iot/platform/
│   │   ├── config/        # Security, Kafka topics, WebSocket config
│   │   ├── controller/    # REST controllers (auth, devices, telemetry, alarms…)
│   │   ├── kafka/         # Kafka consumers
│   │   ├── model/         # Cassandra entity models
│   │   ├── repository/    # Spring Data Cassandra repositories
│   │   ├── security/      # JWT provider, filter, tenant context
│   │   ├── service/       # Business logic
│   │   └── audit/         # Audit logging service
│   ├── Dockerfile
│   └── pom.xml
│
├── bridge/            # MQTT ↔ Kafka bridge (central Python service)
│   ├── bridge.py          # asyncio MQTT subscriber + Kafka producer/consumer
│   ├── config.yaml
│   ├── Dockerfile
│   └── requirements.txt
│
├── device-agent/      # Python device agent (runs on Mini PC)
│   ├── agent.py           # Collects system metrics, publishes to MQTT
│   ├── config.yaml
│   ├── Dockerfile
│   ├── iot-device-agent.service  # systemd unit file
│   └── requirements.txt
│
├── firmware/          # ESP32 Arduino firmware
│   ├── main/
│   │   ├── main.ino       # WiFi, MQTT, telemetry, commands, LWT
│   │   └── config.h       # Device identity & credentials
│   └── platformio.ini
│
├── frontend/          # React + TypeScript + Vite
│   ├── src/
│   │   ├── api/           # Axios client + API endpoints
│   │   ├── auth/          # RBAC guard component
│   │   ├── components/    # TelemetryChart (Recharts)
│   │   ├── layout/        # AppLayout with sidebar navigation
│   │   ├── pages/         # Login, Dashboard, DeviceList, DeviceDetail, Alarms, Admin
│   │   ├── store/         # Zustand stores (auth, device/telemetry state)
│   │   ├── types/         # TypeScript interfaces
│   │   └── websocket/     # STOMP WebSocket hook
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── cassandra/
│   └── init.cql          # Full CQL schema (keyspace, tables, TTL)
│
├── docker-compose.yml    # Full stack: EMQX, Kafka, Zookeeper, Cassandra, Backend, Bridge, Frontend
└── docs/
    └── ARCHITECTURE.md   # Detailed architecture documentation
```

---

## Quick Start (Docker Compose)

### Prerequisites

- Docker & Docker Compose v2
- (Optional) PlatformIO CLI for ESP32 flashing

### 1. Start all services

```bash
docker-compose up -d --build
```

This starts:
| Service | Port | Description |
|---------|------|-------------|
| EMQX | 1883 (MQTT), 18083 (Dashboard) | MQTT broker |
| Kafka | 9092 | Event streaming |
| Cassandra | 9042 | Time-series database |
| Backend | 8080 | Spring Boot REST + WebSocket |
| Frontend | 3000 | React dashboard |
| Bridge | (internal) | MQTT ↔ Kafka bridge |
| Device Agent | (internal) | Example Python device |

### 2. Initialize the database

The `cassandra-init` container automatically runs `cassandra/init.cql`.
Wait ~30s for Cassandra to be healthy.

### 3. Create an initial user

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "tenantId": "00000000-0000-0000-0000-000000000001",
    "username": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "role": "SUPERADMIN"
  }'
```

### 4. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username": "admin", "password": "admin123"}'
```

### 5. Open dashboard

Navigate to **http://localhost:3000** and login.

---

## ESP32 Setup

1. Edit `firmware/main/config.h` with your WiFi, MQTT broker, and device identity.
2. Flash using PlatformIO:

```bash
cd firmware
pio run --target upload
pio device monitor
```

The ESP32 will:
- Connect to WiFi
- Connect to EMQX with LWT
- Publish telemetry every 10s
- Publish heartbeat every 30s
- Listen for commands and send ACKs

---

## Python Mini PC Device Setup

1. Copy `device-agent/` to the mini PC.
2. Edit `config.yaml` with device identity and MQTT credentials.
3. Install & run:

```bash
cd device-agent
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python agent.py
```

Or install as a systemd service:

```bash
sudo cp iot-device-agent.service /etc/systemd/system/
sudo systemctl enable --now iot-device-agent
```

---

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/login` | No | Login, get JWT tokens |
| POST | `/api/auth/register` | No | Register user |
| POST | `/api/auth/refresh` | No | Refresh access token |
| GET | `/api/tenants` | SUPERADMIN | List all tenants |
| POST | `/api/tenants` | SUPERADMIN | Create tenant |
| GET | `/api/tenants/me` | Any | Get current tenant |
| GET | `/api/sites` | Any | List sites |
| POST | `/api/sites` | Any | Create site |
| GET | `/api/devices?siteId=` | Any | List devices by site |
| GET | `/api/devices/:id?siteId=` | Any | Get device detail |
| POST | `/api/devices` | Any | Register device |
| GET | `/api/devices/:id/state` | Any | Get device state |
| GET | `/api/devices/states` | Any | List all device states |
| GET | `/api/telemetry/:deviceId` | Any | Get telemetry data |
| POST | `/api/commands` | OPERATOR+ | Send command to device |
| GET | `/api/commands/:deviceId` | Any | Get command history |
| GET | `/api/alarms` | Any | List alarms |
| POST | `/api/alarms/:id/acknowledge` | OPERATOR+ | Acknowledge alarm |
| GET | `/api/alarms/rules` | Any | List alarm rules |
| POST | `/api/alarms/rules` | ADMIN+ | Create alarm rule |
| GET | `/api/users` | ADMIN+ | List users |
| POST | `/api/users` | ADMIN+ | Create user |
| DELETE | `/api/users/:id` | ADMIN+ | Delete user |

---

## MQTT Topic Design

```
{tenant_id}/{site_id}/{device_id}/telemetry       # device → cloud
{tenant_id}/{site_id}/{device_id}/state            # device → cloud (retained)
{tenant_id}/{site_id}/{device_id}/event            # device → cloud
{tenant_id}/{site_id}/{device_id}/command           # cloud → device
{tenant_id}/{site_id}/{device_id}/command/ack       # device → cloud
{tenant_id}/{site_id}/{device_id}/lwt               # LWT (broker-managed)
```

---

## Kafka Topics

| Topic | Partitions | Retention | Key |
|-------|-----------|-----------|-----|
| `iot.telemetry` | 12 | 7 days | `tenant\|device` |
| `iot.device-state` | 6 | 14 days | `tenant\|device` |
| `iot.events` | 6 | 30 days | `tenant\|device` |
| `iot.commands` | 6 | 3 days | `tenant\|device` |
| `iot.command-acks` | 6 | 3 days | `tenant\|device` |
| `iot.alarms` | 6 | 30 days | `tenant\|device` |
| `iot.audit` | 3 | 90 days | `tenant\|user` |

---

## Security

| Feature | Implementation |
|---------|----------------|
| Authentication | JWT (access 15min + refresh 7d) |
| Authorization | RBAC: SUPERADMIN, TENANT_ADMIN, OPERATOR, VIEWER |
| Tenant isolation | JWT carries tenantId; every query scoped |
| MQTT auth | Username/password per device |
| TLS | EMQX supports TLS on port 8883 |
| Passwords | BCrypt hashed |
| API security | Spring Security filter chain |

---

## Production Deployment

### Kubernetes

- **Backend**: Deployment + HPA (scale on CPU/request rate)
- **Cassandra**: StatefulSet (3+ nodes, NetworkTopologyStrategy RF=3)
- **Kafka**: StatefulSet (3+ brokers, partition replication)
- **EMQX**: StatefulSet with clustering (3-5 nodes)
- **Bridge**: Deployment (2 replicas for HA)
- **Frontend**: Deployment + Ingress with TLS

### Scaling Guide

| Devices | EMQX | Kafka | Cassandra | Backend |
|---------|------|-------|-----------|---------|
| 100 | 1 node | 1 broker | 1 node | 1 pod |
| 1,000 | 2 nodes | 3 brokers | 3 nodes | 2 pods |
| 10,000 | 3 nodes | 3 brokers | 3 nodes | 3 pods |
| 100,000 | 5 nodes | 6 brokers | 6 nodes | 6+ pods + HPA |

### Observability

- **Metrics**: Spring Actuator → Prometheus → Grafana
- **Health**: `/actuator/health` endpoint
- **Kafka lag**: Consumer group lag via metrics
- **EMQX**: Built-in dashboard at `:18083`
- **Logging**: Structured JSON to stdout

---

## License

MIT
