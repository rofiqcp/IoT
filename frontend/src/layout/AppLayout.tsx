import React from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

const AppLayout: React.FC = () => {
  const { role, logout, isAuthenticated } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!isAuthenticated) return <Outlet />;

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      {/* Sidebar */}
      <nav style={{
        width: 220, background: '#1a1a2e', color: '#fff',
        padding: 16, display: 'flex', flexDirection: 'column', gap: 8,
      }}>
        <h3 style={{ marginBottom: 16 }}>IoT Platform</h3>
        <Link to="/" style={{ color: '#aaa', textDecoration: 'none' }}>📊 Dashboard</Link>
        <Link to="/devices" style={{ color: '#aaa', textDecoration: 'none' }}>📱 Devices</Link>
        <Link to="/alarms" style={{ color: '#aaa', textDecoration: 'none' }}>🔔 Alarms</Link>
        {(role === 'SUPERADMIN' || role === 'TENANT_ADMIN') && (
          <Link to="/admin" style={{ color: '#aaa', textDecoration: 'none' }}>⚙️ Admin</Link>
        )}
        <div style={{ marginTop: 'auto' }}>
          <small style={{ color: '#666' }}>Role: {role}</small>
          <br />
          <button onClick={handleLogout} style={{ marginTop: 8, cursor: 'pointer' }}>
            Logout
          </button>
        </div>
      </nav>

      {/* Main content */}
      <main style={{ flex: 1, padding: 24 }}>
        <Outlet />
      </main>
    </div>
  );
};

export default AppLayout;
