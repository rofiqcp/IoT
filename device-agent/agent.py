"""
Python Device Agent — Mini PC IoT Device

Runs on a mini PC and acts as an IoT device:
- Publishes system telemetry (CPU, memory, disk, temperature)
- Publishes online/offline state with LWT
- Listens for commands from the backend
- Sends command ACKs

This is a PEER to ESP32 — both connect directly to EMQX.
"""

import asyncio
import json
import logging
import os
import time

import psutil
import yaml

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
)
log = logging.getLogger("device-agent")


def load_config(path: str = "config.yaml") -> dict:
    with open(path) as f:
        return yaml.safe_load(f)


def collect_metrics(metric_names: list) -> dict:
    """Collect system metrics from the mini PC."""
    data = {}
    for m in metric_names:
        if m == "cpu_percent":
            data["cpu_percent"] = psutil.cpu_percent(interval=0.5)
        elif m == "memory_percent":
            data["memory_percent"] = psutil.virtual_memory().percent
        elif m == "disk_percent":
            data["disk_percent"] = psutil.disk_usage("/").percent
        elif m == "cpu_temperature":
            temps = psutil.sensors_temperatures()
            if temps:
                first_key = next(iter(temps))
                data["cpu_temperature"] = temps[first_key][0].current
            else:
                data["cpu_temperature"] = 0.0
    return data


async def handle_command(payload: str, tenant: str, site: str, device: str, client):
    """Process incoming command and send ACK."""
    try:
        cmd = json.loads(payload)
        cmd_id = cmd.get("commandId", "unknown")
        cmd_type = cmd.get("type", "unknown")
        log.info("Received command: type=%s id=%s", cmd_type, cmd_id)

        # Execute command based on type
        result = "OK"
        if cmd_type == "reboot":
            log.info("Reboot requested — scheduling...")
            result = "REBOOT_SCHEDULED"
        elif cmd_type == "update_interval":
            log.info("Interval update: %s", cmd.get("payload"))
            result = "INTERVAL_UPDATED"
        elif cmd_type == "relay_on":
            log.info("Relay ON (simulated)")
            result = "RELAY_ON"
        elif cmd_type == "relay_off":
            log.info("Relay OFF (simulated)")
            result = "RELAY_OFF"
        else:
            log.warning("Unknown command type: %s", cmd_type)
            result = "UNKNOWN_COMMAND"

        # Send ACK
        ack_topic = f"{tenant}/{site}/{device}/command/ack"
        ack_payload = json.dumps({
            "commandId": cmd_id,
            "status": result,
            "timestamp": int(time.time() * 1000),
        })
        await client.publish(ack_topic, ack_payload, qos=1)
        log.info("ACK sent for command %s", cmd_id)
    except Exception as e:
        log.error("Command handling error: %s", e)


async def run(cfg: dict):
    import aiomqtt

    dev = cfg["device"]
    mqtt_cfg = cfg["mqtt"]
    tel_cfg = cfg["telemetry"]
    hb_cfg = cfg["heartbeat"]

    tenant = dev["tenant_id"]
    site = dev["site_id"]
    device = dev["device_id"]

    # Topic paths
    telemetry_topic = f"{tenant}/{site}/{device}/telemetry"
    state_topic = f"{tenant}/{site}/{device}/state"
    command_topic = f"{tenant}/{site}/{device}/command"
    lwt_topic = f"{tenant}/{site}/{device}/lwt"

    # LWT payload — broker publishes this if device disconnects unexpectedly
    lwt_payload = json.dumps({
        "deviceId": device,
        "tenantId": tenant,
        "siteId": site,
        "online": False,
        "timestamp": int(time.time() * 1000),
    })

    while True:
        try:
            async with aiomqtt.Client(
                hostname=mqtt_cfg["host"],
                port=mqtt_cfg["port"],
                username=mqtt_cfg.get("username"),
                password=mqtt_cfg.get("password"),
                identifier=f"{tenant}-{device}",
                keepalive=mqtt_cfg.get("keepalive", 60),
                will=aiomqtt.Will(
                    topic=lwt_topic,
                    payload=lwt_payload.encode(),
                    qos=1,
                    retain=True,
                ),
            ) as client:
                # Publish online state
                online_payload = json.dumps({
                    "deviceId": device,
                    "tenantId": tenant,
                    "siteId": site,
                    "online": True,
                    "timestamp": int(time.time() * 1000),
                })
                await client.publish(state_topic, online_payload, qos=1, retain=True)
                log.info("Device %s online, publishing telemetry to %s", device, telemetry_topic)

                # Subscribe to commands
                await client.subscribe(command_topic, qos=1)
                log.info("Subscribed to %s", command_topic)

                async def telemetry_loop():
                    while True:
                        metrics = collect_metrics(tel_cfg.get("metrics", []))
                        for metric_name, metric_value in metrics.items():
                            payload = json.dumps({
                                "deviceId": device,
                                "tenantId": tenant,
                                "metric": metric_name,
                                "value": metric_value,
                                "unit": "%" if "percent" in metric_name else "°C",
                                "timestamp": int(time.time() * 1000),
                            })
                            await client.publish(telemetry_topic, payload, qos=1)
                        log.debug("Telemetry published: %s", metrics)
                        await asyncio.sleep(tel_cfg.get("interval_seconds", 10))

                async def heartbeat_loop():
                    while True:
                        hb = json.dumps({
                            "deviceId": device,
                            "tenantId": tenant,
                            "siteId": site,
                            "online": True,
                            "timestamp": int(time.time() * 1000),
                        })
                        await client.publish(state_topic, hb, qos=1, retain=True)
                        await asyncio.sleep(hb_cfg.get("interval_seconds", 30))

                async def command_listener():
                    async for msg in client.messages:
                        await handle_command(
                            msg.payload.decode(), tenant, site, device, client
                        )

                await asyncio.gather(
                    telemetry_loop(),
                    heartbeat_loop(),
                    command_listener(),
                )

        except Exception as e:
            log.error("Connection lost (%s), reconnecting in 5s...", e)
            await asyncio.sleep(5)


def main():
    cfg = load_config(os.environ.get("DEVICE_CONFIG", "config.yaml"))
    log.info("Starting Python Device Agent: %s", cfg["device"]["device_id"])
    asyncio.run(run(cfg))


if __name__ == "__main__":
    main()
