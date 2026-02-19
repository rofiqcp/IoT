import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import type { Role } from '../types';

interface Props {
  children: React.ReactNode;
  allowed?: Role[];
}

/**
 * RBAC guard — wraps routes that require authentication and specific roles.
 */
export const RBACGuard: React.FC<Props> = ({ children, allowed }) => {
  const { isAuthenticated, role } = useAuthStore();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowed && role && !allowed.includes(role)) {
    return (
      <div style={{ padding: 40, textAlign: 'center' }}>
        <h2>403 — Access Denied</h2>
        <p>You do not have permission to view this page.</p>
      </div>
    );
  }

  return <>{children}</>;
};
