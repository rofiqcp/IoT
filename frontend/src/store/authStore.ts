import { create } from 'zustand';
import type { Role } from '../types';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  role: Role | null;
  tenantId: string | null;
  isAuthenticated: boolean;
  login: (tokens: { accessToken: string; refreshToken: string; role: string; tenantId: string }) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: localStorage.getItem('accessToken'),
  refreshToken: localStorage.getItem('refreshToken'),
  role: localStorage.getItem('role') as Role | null,
  tenantId: localStorage.getItem('tenantId'),
  isAuthenticated: !!localStorage.getItem('accessToken'),

  login: (tokens) => {
    localStorage.setItem('accessToken', tokens.accessToken);
    localStorage.setItem('refreshToken', tokens.refreshToken);
    localStorage.setItem('role', tokens.role);
    localStorage.setItem('tenantId', tokens.tenantId);
    set({
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
      role: tokens.role as Role,
      tenantId: tokens.tenantId,
      isAuthenticated: true,
    });
  },

  logout: () => {
    localStorage.clear();
    set({
      accessToken: null,
      refreshToken: null,
      role: null,
      tenantId: null,
      isAuthenticated: false,
    });
  },
}));
