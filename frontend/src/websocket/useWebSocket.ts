import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuthStore } from '../store/authStore';
import { useDeviceStore } from '../store/deviceStore';

/**
 * Hook that connects to the backend WebSocket (STOMP over SockJS)
 * and pushes real-time telemetry + state updates into Zustand stores.
 */
export function useWebSocket() {
  const tenantId = useAuthStore((s) => s.tenantId);
  const { updateState, appendTelemetry } = useDeviceStore();
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!tenantId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        // Subscribe to all telemetry for this tenant (wildcard via backend broadcast)
        client.subscribe(`/topic/telemetry/${tenantId}/*`, (message) => {
          try {
            const data = JSON.parse(message.body);
            appendTelemetry(data.deviceId, {
              tenantId: data.tenantId,
              deviceId: data.deviceId,
              dayBucket: '',
              recordedAt: new Date(data.timestamp).toISOString(),
              metricName: data.metric,
              metricValue: data.value,
              unit: data.unit || '',
            });
          } catch { /* ignore parse errors */ }
        });

        client.subscribe(`/topic/state/${tenantId}/*`, (message) => {
          try {
            const data = JSON.parse(message.body);
            updateState({
              tenantId: data.tenantId,
              deviceId: data.deviceId,
              online: data.online,
              lastSeen: new Date(data.timestamp).toISOString(),
              ipAddress: data.ip,
              rssi: data.rssi,
            });
          } catch { /* ignore parse errors */ }
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [tenantId, updateState, appendTelemetry]);
}
