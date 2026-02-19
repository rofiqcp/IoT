import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getDevices, getDeviceStates, getSites } from '../api/endpoints';
import { useDeviceStore } from '../store/deviceStore';
import type { Site } from '../types';

const DeviceListPage: React.FC = () => {
  const { devices, states, setDevices, setStates } = useDeviceStore();
  const [sites, setSites] = useState<Site[]>([]);
  const [selectedSite, setSelectedSite] = useState('');

  useEffect(() => {
    getSites().then((r) => {
      setSites(r.data);
      if (r.data.length > 0) setSelectedSite(r.data[0].siteId);
    });
    getDeviceStates().then((r) => setStates(r.data));
  }, [setStates]);

  useEffect(() => {
    if (selectedSite) {
      getDevices(selectedSite).then((r) => setDevices(r.data));
    }
  }, [selectedSite, setDevices]);

  return (
    <div>
      <h2>Devices</h2>

      <div style={{ marginBottom: 16 }}>
        <label>Site: </label>
        <select value={selectedSite} onChange={(e) => setSelectedSite(e.target.value)}>
          {sites.map((s) => (
            <option key={s.siteId} value={s.siteId}>{s.name}</option>
          ))}
        </select>
      </div>

      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ borderBottom: '2px solid #333' }}>
            <th style={{ textAlign: 'left', padding: 8 }}>Name</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Type</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Status</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Last Seen</th>
            <th style={{ textAlign: 'left', padding: 8 }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {devices.map((d) => {
            const state = states[d.deviceId];
            return (
              <tr key={d.deviceId} style={{ borderBottom: '1px solid #ccc' }}>
                <td style={{ padding: 8 }}>{d.name}</td>
                <td style={{ padding: 8 }}>{d.deviceType}</td>
                <td style={{ padding: 8 }}>
                  <span style={{
                    color: state?.online ? 'green' : 'red',
                    fontWeight: 'bold',
                  }}>
                    {state?.online ? '● Online' : '○ Offline'}
                  </span>
                </td>
                <td style={{ padding: 8 }}>
                  {state?.lastSeen ? new Date(state.lastSeen).toLocaleString() : '—'}
                </td>
                <td style={{ padding: 8 }}>
                  <Link to={`/devices/${d.deviceId}?siteId=${d.siteId}`}>Detail</Link>
                </td>
              </tr>
            );
          })}
          {devices.length === 0 && (
            <tr><td colSpan={5} style={{ padding: 16, textAlign: 'center' }}>No devices</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default DeviceListPage;
