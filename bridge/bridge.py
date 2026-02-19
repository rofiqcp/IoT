"""
MQTT-Kafka Bridge — Central service.

Bridges all device MQTT messages to Kafka topics, and
forwards commands from Kafka back to devices via MQTT.

Both ESP32 and Python Mini PC devices connect directly to EMQX;
this bridge moves data between EMQX and Kafka.
"""

import asyncio
import json
import logging
import os
import signal
import sys
import time
from pathlib import Path

import yaml

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
)
log = logging.getLogger("bridge")

# ---------------------------------------------------------------------------
# Config
# ---------------------------------------------------------------------------

def load_config(path: str = "config.yaml") -> dict:
    with open(path) as f:
        return yaml.safe_load(f)

# ---------------------------------------------------------------------------
# Disk buffer (simple append-ahead log for offline resilience)
# ---------------------------------------------------------------------------

class DiskBuffer:
    """Append-only file buffer used when Kafka is temporarily unreachable."""

    def __init__(self, path: str, max_bytes: int):
        self.path = Path(path)
        self.path.mkdir(parents=True, exist_ok=True)
        self.file = self.path / "pending.jsonl"
        self.max_bytes = max_bytes

    def append(self, record: dict):
        line = json.dumps(record) + "\n"
        if self.file.exists() and self.file.stat().st_size > self.max_bytes:
            log.warning("Disk buffer full, dropping oldest entries")
            lines = self.file.read_text().splitlines()
            self.file.write_text("\n".join(lines[len(lines) // 2 :]) + "\n")
        with open(self.file, "a") as f:
            f.write(line)

    def drain(self):
        """Yield buffered records and clear the file."""
        if not self.file.exists():
            return
        with open(self.file) as f:
            for line in f:
                line = line.strip()
                if line:
                    yield json.loads(line)
        self.file.unlink(missing_ok=True)

# ---------------------------------------------------------------------------
# Kafka helpers (synchronous confluent-kafka under asyncio via executor)
# ---------------------------------------------------------------------------

from confluent_kafka import Producer as KafkaProducer
from confluent_kafka import Consumer as KafkaConsumer

def make_kafka_producer(bootstrap: str) -> KafkaProducer:
    return KafkaProducer({
        "bootstrap.servers": bootstrap,
        "acks": "all",
        "enable.idempotence": True,
        "retries": 5,
        "linger.ms": 10,
    })

def kafka_produce(producer: KafkaProducer, topic: str, key: str, value: str):
    producer.produce(topic, key=key.encode(), value=value.encode())
    producer.poll(0)

def make_kafka_consumer(bootstrap: str, group: str, topics: list) -> KafkaConsumer:
    consumer = KafkaConsumer({
        "bootstrap.servers": bootstrap,
        "group.id": group,
        "auto.offset.reset": "latest",
        "enable.auto.commit": True,
    })
    consumer.subscribe(topics)
    return consumer

# ---------------------------------------------------------------------------
# MQTT → Kafka  (telemetry, state, events, acks flow)
# ---------------------------------------------------------------------------

async def mqtt_to_kafka(cfg: dict, buffer: DiskBuffer):
    """Subscribe to EMQX wildcard topics and forward to Kafka."""
    import aiomqtt

    kafka_cfg = cfg["kafka"]
    mqtt_cfg = cfg["mqtt"]
    topic_map = kafka_cfg["topic_map"]

    producer = make_kafka_producer(kafka_cfg["bootstrap_servers"])

    # Drain any buffered records from previous crash
    for rec in buffer.drain():
        try:
            kafka_produce(producer, rec["topic"], rec["key"], rec["value"])
            log.info("Drained buffered record to %s", rec["topic"])
        except Exception as e:
            log.error("Failed to drain buffer record: %s", e)

    while True:
        try:
            async with aiomqtt.Client(
                hostname=mqtt_cfg["host"],
                port=mqtt_cfg["port"],
                username=mqtt_cfg.get("username"),
                password=mqtt_cfg.get("password"),
                identifier=mqtt_cfg["client_id"] + "-sub",
            ) as client:
                for t in mqtt_cfg["subscribe_topics"]:
                    await client.subscribe(t, qos=1)
                    log.info("Subscribed to %s", t)

                async for msg in client.messages:
                    topic_str = str(msg.topic)
                    parts = topic_str.split("/")
                    # Expected: {tenant_id}/{site_id}/{device_id}/{suffix...}
                    if len(parts) < 4:
                        continue
                    tenant_id, site_id, device_id = parts[0], parts[1], parts[2]
                    suffix = "/".join(parts[3:])

                    kafka_topic = topic_map.get(suffix)
                    if not kafka_topic:
                        log.debug("No Kafka mapping for suffix: %s", suffix)
                        continue

                    payload = msg.payload.decode() if isinstance(msg.payload, bytes) else str(msg.payload)

                    # Enrich with identifiers if not present
                    try:
                        data = json.loads(payload)
                    except json.JSONDecodeError:
                        data = {"raw": payload}
                    data.setdefault("tenantId", tenant_id)
                    data.setdefault("siteId", site_id)
                    data.setdefault("deviceId", device_id)
                    data.setdefault("timestamp", int(time.time() * 1000))

                    key = f"{tenant_id}|{device_id}"
                    value = json.dumps(data)

                    try:
                        kafka_produce(producer, kafka_topic, key, value)
                    except Exception:
                        log.warning("Kafka unreachable, buffering to disk")
                        buffer.append({"topic": kafka_topic, "key": key, "value": value})

        except Exception as e:
            log.error("MQTT connection lost (%s), reconnecting in 5s...", e)
            await asyncio.sleep(5)

# ---------------------------------------------------------------------------
# Kafka → MQTT  (command flow: backend → device)
# ---------------------------------------------------------------------------

async def kafka_to_mqtt(cfg: dict):
    """Consume commands from Kafka and publish to device MQTT topics."""
    import aiomqtt

    kafka_cfg = cfg["kafka"]
    mqtt_cfg = cfg["mqtt"]

    consumer = make_kafka_consumer(
        kafka_cfg["bootstrap_servers"],
        kafka_cfg["consumer_group"],
        [kafka_cfg["command_topic"]],
    )

    loop = asyncio.get_event_loop()

    while True:
        try:
            async with aiomqtt.Client(
                hostname=mqtt_cfg["host"],
                port=mqtt_cfg["port"],
                username=mqtt_cfg.get("username"),
                password=mqtt_cfg.get("password"),
                identifier=mqtt_cfg["client_id"] + "-cmd",
            ) as client:
                log.info("Command bridge connected to MQTT")
                while True:
                    msg = await loop.run_in_executor(None, lambda: consumer.poll(1.0))
                    if msg is None:
                        continue
                    if msg.error():
                        log.error("Kafka consumer error: %s", msg.error())
                        continue
                    try:
                        data = json.loads(msg.value().decode())
                        tenant_id = data.get("tenantId", "unknown")
                        device_id = data.get("deviceId", "unknown")
                        site_id = data.get("siteId", "default")
                        mqtt_topic = f"{tenant_id}/{site_id}/{device_id}/command"
                        await client.publish(mqtt_topic, json.dumps(data), qos=1)
                        log.info("Command sent to %s", mqtt_topic)
                    except Exception as e:
                        log.error("Error publishing command to MQTT: %s", e)
        except Exception as e:
            log.error("MQTT command bridge lost (%s), reconnecting in 5s...", e)
            await asyncio.sleep(5)

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

async def main():
    cfg = load_config(os.environ.get("BRIDGE_CONFIG", "config.yaml"))
    buf_cfg = cfg.get("buffer", {})
    buffer = DiskBuffer(
        buf_cfg.get("path", "/tmp/bridge_buffer"),
        buf_cfg.get("max_size_mb", 512) * 1024 * 1024,
    )

    log.info("Starting MQTT-Kafka Bridge")
    await asyncio.gather(
        mqtt_to_kafka(cfg, buffer),
        kafka_to_mqtt(cfg),
    )

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        log.info("Bridge stopped")
