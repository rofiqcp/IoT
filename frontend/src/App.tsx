import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppLayout from './layout/AppLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import DeviceListPage from './pages/DeviceListPage';
import DeviceDetailPage from './pages/DeviceDetailPage';
import AlarmPage from './pages/AlarmPage';
import AdminPage from './pages/AdminPage';
import { RBACGuard } from './auth/RBACGuard';
import { useWebSocket } from './websocket/useWebSocket';

const App: React.FC = () => {
  useWebSocket();

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<AppLayout />}>
          <Route path="/" element={<RBACGuard><DashboardPage /></RBACGuard>} />
          <Route path="/devices" element={<RBACGuard><DeviceListPage /></RBACGuard>} />
          <Route path="/devices/:deviceId" element={<RBACGuard><DeviceDetailPage /></RBACGuard>} />
          <Route path="/alarms" element={<RBACGuard><AlarmPage /></RBACGuard>} />
          <Route path="/admin" element={
            <RBACGuard allowed={['SUPERADMIN', 'TENANT_ADMIN']}>
              <AdminPage />
            </RBACGuard>
          } />
        </Route>
      </Routes>
    </BrowserRouter>
  );
};

export default App;
