// ============================================================
// config.h — Device configuration (set per device at flash time)
// ============================================================
#ifndef CONFIG_H
#define CONFIG_H

// --- WiFi (override at build time: -DWIFI_SSID=\"MySSID\") ---
#ifndef WIFI_SSID
#define WIFI_SSID        "YourWiFiSSID"
#endif
#ifndef WIFI_PASSWORD
#define WIFI_PASSWORD    "YourWiFiPassword"
#endif

// --- MQTT Broker (override at build time or via provisioning) ---
#ifndef MQTT_HOST
#define MQTT_HOST        "emqx.example.com"
#endif
#define MQTT_PORT        1883          // Use 8883 for TLS
#ifndef MQTT_USERNAME
#define MQTT_USERNAME    "tenant-abc:esp32-001"
#endif
#ifndef MQTT_PASSWORD
#define MQTT_PASSWORD    "device_secret_key"
#endif

// --- Device Identity ---
#define TENANT_ID        "tenant-abc"
#define SITE_ID          "site-01"
#define DEVICE_ID        "esp32-001"

// --- Intervals ---
#define TELEMETRY_INTERVAL_MS   10000   // 10 seconds
#define HEARTBEAT_INTERVAL_MS   30000   // 30 seconds

// --- Hardware ---
#define RELAY_PIN        26             // GPIO for relay control
#define SENSOR_PIN       34             // GPIO for analog sensor (ADC)
#define LED_PIN          2              // Built-in LED

#endif // CONFIG_H
