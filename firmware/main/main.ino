/*
 * ESP32 IoT Device Firmware
 *
 * Connects directly to the central EMQX broker.
 * Publishes telemetry and heartbeat state.
 * Subscribes to command topic and sends ACKs.
 * Uses LWT for offline detection.
 *
 * This is a PEER device alongside Python Mini PC — both connect
 * to the same EMQX cluster using the same topic scheme.
 */

#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "config.h"

// --- Globals ---
WiFiClient   wifiClient;
PubSubClient mqttClient(wifiClient);

unsigned long lastTelemetry = 0;
unsigned long lastHeartbeat = 0;
unsigned long telemetryInterval = TELEMETRY_INTERVAL_MS;

// --- Topic buffers ---
char topicTelemetry[128];
char topicState[128];
char topicCommand[128];
char topicCommandAck[128];
char topicLWT[128];

// ============================================================
// WiFi
// ============================================================

void setupWiFi() {
    Serial.print("Connecting to WiFi");
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.printf("\nWiFi connected — IP: %s\n", WiFi.localIP().toString().c_str());
}

// ============================================================
// MQTT topics
// ============================================================

void buildTopics() {
    snprintf(topicTelemetry,  sizeof(topicTelemetry),
             "%s/%s/%s/telemetry", TENANT_ID, SITE_ID, DEVICE_ID);
    snprintf(topicState,      sizeof(topicState),
             "%s/%s/%s/state",     TENANT_ID, SITE_ID, DEVICE_ID);
    snprintf(topicCommand,    sizeof(topicCommand),
             "%s/%s/%s/command",   TENANT_ID, SITE_ID, DEVICE_ID);
    snprintf(topicCommandAck, sizeof(topicCommandAck),
             "%s/%s/%s/command/ack", TENANT_ID, SITE_ID, DEVICE_ID);
    snprintf(topicLWT,        sizeof(topicLWT),
             "%s/%s/%s/lwt",       TENANT_ID, SITE_ID, DEVICE_ID);
}

// ============================================================
// Command handler
// ============================================================

void onCommand(char* topic, byte* payload, unsigned int length) {
    char msg[512];
    if (length >= sizeof(msg)) length = sizeof(msg) - 1;
    memcpy(msg, payload, length);
    msg[length] = '\0';

    Serial.printf("Command received: %s\n", msg);

    JsonDocument doc;
    DeserializationError err = deserializeJson(doc, msg);
    if (err) {
        Serial.printf("JSON parse error: %s\n", err.c_str());
        return;
    }

    const char* cmdId   = doc["commandId"] | "unknown";
    const char* cmdType = doc["type"]      | "unknown";
    const char* result  = "OK";

    // --- Execute command ---
    if (strcmp(cmdType, "relay_on") == 0) {
        digitalWrite(RELAY_PIN, HIGH);
        result = "RELAY_ON";
    } else if (strcmp(cmdType, "relay_off") == 0) {
        digitalWrite(RELAY_PIN, LOW);
        result = "RELAY_OFF";
    } else if (strcmp(cmdType, "update_interval") == 0) {
        unsigned long newInterval = doc["payload"]["interval"] | TELEMETRY_INTERVAL_MS;
        telemetryInterval = newInterval;
        result = "INTERVAL_UPDATED";
    } else if (strcmp(cmdType, "reboot") == 0) {
        result = "REBOOTING";
        // Send ACK before reboot
        JsonDocument ack;
        ack["commandId"] = cmdId;
        ack["status"]    = result;
        ack["timestamp"] = millis();
        char ackBuf[256];
        serializeJson(ack, ackBuf);
        mqttClient.publish(topicCommandAck, ackBuf);
        delay(500);
        ESP.restart();
        return;
    } else {
        result = "UNKNOWN_COMMAND";
    }

    // --- Send ACK ---
    JsonDocument ack;
    ack["commandId"] = cmdId;
    ack["status"]    = result;
    ack["timestamp"] = millis();
    char ackBuf[256];
    serializeJson(ack, ackBuf);
    mqttClient.publish(topicCommandAck, ackBuf);
    Serial.printf("ACK sent: %s\n", ackBuf);
}

// ============================================================
// MQTT connection (with LWT)
// ============================================================

void connectMQTT() {
    mqttClient.setServer(MQTT_HOST, MQTT_PORT);
    mqttClient.setCallback(onCommand);
    mqttClient.setBufferSize(1024);

    // LWT payload
    JsonDocument lwtDoc;
    lwtDoc["deviceId"] = DEVICE_ID;
    lwtDoc["tenantId"] = TENANT_ID;
    lwtDoc["siteId"]   = SITE_ID;
    lwtDoc["online"]   = false;
    char lwtBuf[256];
    serializeJson(lwtDoc, lwtBuf);

    while (!mqttClient.connected()) {
        Serial.print("Connecting to MQTT...");
        String clientId = String(TENANT_ID) + "-" + String(DEVICE_ID);
        if (mqttClient.connect(clientId.c_str(), MQTT_USERNAME, MQTT_PASSWORD,
                                topicLWT, 1, true, lwtBuf)) {
            Serial.println(" connected!");

            // Publish online state (retained)
            JsonDocument stateDoc;
            stateDoc["deviceId"] = DEVICE_ID;
            stateDoc["tenantId"] = TENANT_ID;
            stateDoc["siteId"]   = SITE_ID;
            stateDoc["online"]   = true;
            stateDoc["ip"]       = WiFi.localIP().toString();
            stateDoc["rssi"]     = WiFi.RSSI();
            char stateBuf[256];
            serializeJson(stateDoc, stateBuf);
            mqttClient.publish(topicState, stateBuf, true);

            // Subscribe to commands
            mqttClient.subscribe(topicCommand, 1);
            Serial.printf("Subscribed to %s\n", topicCommand);
        } else {
            Serial.printf(" failed (rc=%d), retrying in 5s\n", mqttClient.state());
            delay(5000);
        }
    }
}

// ============================================================
// Telemetry publishing
// ============================================================

void publishTelemetry() {
    // Read analog sensor (simulated temperature)
    int raw = analogRead(SENSOR_PIN);
    float temperature = (raw / 4095.0) * 100.0;  // map to 0-100

    // Publish temperature metric
    JsonDocument doc;
    doc["deviceId"]  = DEVICE_ID;
    doc["tenantId"]  = TENANT_ID;
    doc["metric"]    = "temperature";
    doc["value"]     = temperature;
    doc["unit"]      = "°C";
    doc["timestamp"] = millis();
    char buf[256];
    serializeJson(doc, buf);
    mqttClient.publish(topicTelemetry, buf);

    // Publish WiFi RSSI as a metric
    JsonDocument rssiDoc;
    rssiDoc["deviceId"]  = DEVICE_ID;
    rssiDoc["tenantId"]  = TENANT_ID;
    rssiDoc["metric"]    = "rssi";
    rssiDoc["value"]     = WiFi.RSSI();
    rssiDoc["unit"]      = "dBm";
    rssiDoc["timestamp"] = millis();
    char rssiBuf[256];
    serializeJson(rssiDoc, rssiBuf);
    mqttClient.publish(topicTelemetry, rssiBuf);

    // Publish relay state
    JsonDocument relayDoc;
    relayDoc["deviceId"]  = DEVICE_ID;
    relayDoc["tenantId"]  = TENANT_ID;
    relayDoc["metric"]    = "relay_state";
    relayDoc["value"]     = digitalRead(RELAY_PIN);
    relayDoc["unit"]      = "bool";
    relayDoc["timestamp"] = millis();
    char relayBuf[256];
    serializeJson(relayDoc, relayBuf);
    mqttClient.publish(topicTelemetry, relayBuf);
}

// ============================================================
// Heartbeat
// ============================================================

void publishHeartbeat() {
    JsonDocument doc;
    doc["deviceId"]  = DEVICE_ID;
    doc["tenantId"]  = TENANT_ID;
    doc["siteId"]    = SITE_ID;
    doc["online"]    = true;
    doc["rssi"]      = WiFi.RSSI();
    doc["ip"]        = WiFi.localIP().toString();
    doc["timestamp"] = millis();
    char buf[256];
    serializeJson(doc, buf);
    mqttClient.publish(topicState, buf, true);  // retained
}

// ============================================================
// Setup & Loop
// ============================================================

void setup() {
    Serial.begin(115200);
    delay(1000);

    pinMode(RELAY_PIN, OUTPUT);
    pinMode(LED_PIN, OUTPUT);
    digitalWrite(RELAY_PIN, LOW);

    buildTopics();
    setupWiFi();
    connectMQTT();

    Serial.println("ESP32 IoT device ready");
}

void loop() {
    if (!mqttClient.connected()) {
        connectMQTT();
    }
    mqttClient.loop();

    unsigned long now = millis();

    if (now - lastTelemetry >= telemetryInterval) {
        lastTelemetry = now;
        publishTelemetry();
        digitalWrite(LED_PIN, !digitalRead(LED_PIN));  // blink LED
    }

    if (now - lastHeartbeat >= HEARTBEAT_INTERVAL_MS) {
        lastHeartbeat = now;
        publishHeartbeat();
    }
}
