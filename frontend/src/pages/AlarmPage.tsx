import React, { useEffect } from 'react';
import { getAlarms, acknowledgeAlarm } from '../api/endpoints';
import { useDeviceStore } from '../store/deviceStore';

const AlarmPage: React.FC = () => {
  const { alarms, setAlarms } = useDeviceStore();

  useEffect(() => {
    getAlarms(200).then((r) => setAlarms(r.data));
  }, [setAlarms]);

  const handleAck = async (alarmId: string) => {
    await acknowledgeAlarm(alarmId);
    getAlarms(200).then((r) => setAlarms(r.data));
  };

  return (
    <div>
      <h2>Alarm Dashboard</h2>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ borderBottom: '2px solid #333' }}>
            <th style={{ textAlign: 'left', padding: 8 }}>Severity</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Rule</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Message</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Device</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Triggered</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Status</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Action</th>
          </tr>
        </thead>
        <tbody>
          {alarms.map((a) => (
            <tr key={a.alarmId} style={{
              borderBottom: '1px solid #ccc',
              background: a.active ? (a.severity === 'CRITICAL' ? '#ffe0e0' : '#fff8e0') : undefined,
            }}>
              <td style={{ padding: 8, fontWeight: 'bold' }}>{a.severity}</td>
              <td style={{ padding: 8 }}>{a.ruleName}</td>
              <td style={{ padding: 8 }}>{a.message}</td>
              <td style={{ padding: 8, fontFamily: 'monospace' }}>{a.deviceId?.slice(0, 8)}</td>
              <td style={{ padding: 8 }}>{new Date(a.triggeredAt).toLocaleString()}</td>
              <td style={{ padding: 8 }}>{a.active ? 'ACTIVE' : 'ACK'}</td>
              <td style={{ padding: 8 }}>
                {a.active && (
                  <button onClick={() => handleAck(a.alarmId)}>Acknowledge</button>
                )}
              </td>
            </tr>
          ))}
          {alarms.length === 0 && (
            <tr><td colSpan={7} style={{ padding: 16, textAlign: 'center' }}>No alarms</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default AlarmPage;
