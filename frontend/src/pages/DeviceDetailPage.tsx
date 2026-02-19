import React, { useEffect, useState } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import {
  getTelemetry, getDevice, sendCommand, getCommands,
} from '../api/endpoints';
import { useDeviceStore } from '../store/deviceStore';
import TelemetryChart from '../components/TelemetryChart';
import type { Device, Command } from '../types';

const DeviceDetailPage: React.FC = () => {
  const { deviceId } = useParams<{ deviceId: string }>();
  const [searchParams] = useSearchParams();
  const siteId = searchParams.get('siteId') || '';
  const [device, setDevice] = useState<Device | null>(null);
  const [commands, setCommands] = useState<Command[]>([]);
  const [cmdType, setCmdType] = useState('relay_on');
  const [cmdPayload, setCmdPayload] = useState('{}');
  const { telemetry, setTelemetry, states } = useDeviceStore();

  useEffect(() => {
    if (!deviceId || !siteId) return;
    getDevice(deviceId, siteId).then((r) => setDevice(r.data));
    getTelemetry(deviceId).then((r) => setTelemetry(deviceId, r.data));
    getCommands(deviceId).then((r) => setCommands(r.data));
  }, [deviceId, siteId, setTelemetry]);

  const handleSendCommand = async () => {
    if (!deviceId) return;
    await sendCommand(deviceId, cmdType, cmdPayload);
    getCommands(deviceId).then((r) => setCommands(r.data));
  };

  const state = deviceId ? states[deviceId] : null;
  const records = deviceId ? (telemetry[deviceId] || []) : [];

  return (
    <div>
      <h2>Device: {device?.name || deviceId}</h2>

      <div style={{ display: 'flex', gap: 24, marginBottom: 16 }}>
        <div>
          <strong>Type:</strong> {device?.deviceType}
        </div>
        <div>
          <strong>Status:</strong>{' '}
          <span style={{ color: state?.online ? 'green' : 'red' }}>
            {state?.online ? '● Online' : '○ Offline'}
          </span>
        </div>
        <div>
          <strong>IP:</strong> {state?.ipAddress || '—'}
        </div>
        <div>
          <strong>RSSI:</strong> {state?.rssi ?? '—'} dBm
        </div>
      </div>

      {/* Telemetry Chart */}
      <h3>Telemetry</h3>
      <TelemetryChart records={records} />

      {/* Command Panel */}
      <h3>Send Command</h3>
      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        <select value={cmdType} onChange={(e) => setCmdType(e.target.value)}>
          <option value="relay_on">Relay ON</option>
          <option value="relay_off">Relay OFF</option>
          <option value="reboot">Reboot</option>
          <option value="update_interval">Update Interval</option>
        </select>
        <input
          type="text"
          value={cmdPayload}
          onChange={(e) => setCmdPayload(e.target.value)}
          placeholder='Payload JSON'
          style={{ width: 200, padding: 4 }}
        />
        <button onClick={handleSendCommand}>Send</button>
      </div>

      {/* Command History */}
      <h3>Command History</h3>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ borderBottom: '2px solid #333' }}>
            <th style={{ textAlign: 'left', padding: 8 }}>Type</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Status</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Created</th>
          </tr>
        </thead>
        <tbody>
          {commands.map((c) => (
            <tr key={c.commandId} style={{ borderBottom: '1px solid #ccc' }}>
              <td style={{ padding: 8 }}>{c.commandType}</td>
              <td style={{ padding: 8 }}>{c.status}</td>
              <td style={{ padding: 8 }}>{new Date(c.createdAt).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default DeviceDetailPage;
