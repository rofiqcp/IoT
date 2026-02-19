import React, { useEffect, useState } from 'react';
import { getDeviceStates, getAlarms } from '../api/endpoints';
import { useDeviceStore } from '../store/deviceStore';

const DashboardPage: React.FC = () => {
  const { states, alarms, setStates, setAlarms } = useDeviceStore();
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    Promise.all([
      getDeviceStates().then((r) => setStates(r.data)),
      getAlarms(20).then((r) => setAlarms(r.data)),
    ]).then(() => setLoaded(true));
  }, [setStates, setAlarms]);

  const stateList = Object.values(states);
  const onlineCount = stateList.filter((s) => s.online).length;
  const offlineCount = stateList.length - onlineCount;
  const activeAlarms = alarms.filter((a) => a.active).length;

  return (
    <div>
      <h2>Dashboard</h2>

      {!loaded ? (
        <p>Loading...</p>
      ) : (
        <>
          <div style={{ display: 'flex', gap: 24, marginBottom: 32 }}>
            <div style={{ padding: 24, background: '#e8f5e9', borderRadius: 8, flex: 1, textAlign: 'center' }}>
              <h1 style={{ margin: 0, color: 'green' }}>{onlineCount}</h1>
              <p>Devices Online</p>
            </div>
            <div style={{ padding: 24, background: '#ffebee', borderRadius: 8, flex: 1, textAlign: 'center' }}>
              <h1 style={{ margin: 0, color: 'red' }}>{offlineCount}</h1>
              <p>Devices Offline</p>
            </div>
            <div style={{ padding: 24, background: '#fff8e1', borderRadius: 8, flex: 1, textAlign: 'center' }}>
              <h1 style={{ margin: 0, color: '#ff8f00' }}>{activeAlarms}</h1>
              <p>Active Alarms</p>
            </div>
            <div style={{ padding: 24, background: '#e3f2fd', borderRadius: 8, flex: 1, textAlign: 'center' }}>
              <h1 style={{ margin: 0, color: '#1565c0' }}>{stateList.length}</h1>
              <p>Total Devices</p>
            </div>
          </div>

          {activeAlarms > 0 && (
            <div>
              <h3>Recent Alarms</h3>
              <ul>
                {alarms.filter((a) => a.active).slice(0, 5).map((a) => (
                  <li key={a.alarmId}>
                    <strong>[{a.severity}]</strong> {a.message} — Device {a.deviceId?.slice(0, 8)}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default DashboardPage;
