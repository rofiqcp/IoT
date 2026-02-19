// Types used across the frontend

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  role: string;
  tenantId: string;
}

export type Role = 'SUPERADMIN' | 'TENANT_ADMIN' | 'OPERATOR' | 'VIEWER';

export interface Tenant {
  tenantId: string;
  name: string;
  createdAt: string;
}

export interface Site {
  tenantId: string;
  siteId: string;
  name: string;
  location: string;
  createdAt: string;
}

export interface Device {
  tenantId: string;
  siteId: string;
  deviceId: string;
  areaId?: string;
  lineId?: string;
  name: string;
  deviceType: string;
  credentialKey?: string;
  firmwareVersion?: string;
  createdAt: string;
}

export interface DeviceState {
  tenantId: string;
  deviceId: string;
  online: boolean;
  lastSeen: string;
  ipAddress?: string;
  rssi?: number;
}

export interface TelemetryRecord {
  tenantId: string;
  deviceId: string;
  dayBucket: string;
  recordedAt: string;
  metricName: string;
  metricValue: number;
  unit: string;
}

export interface Alarm {
  alarmId: string;
  tenantId: string;
  deviceId: string;
  ruleName: string;
  severity: string;
  message: string;
  active: boolean;
  triggeredAt: string;
  acknowledgedAt?: string;
  acknowledgedBy?: string;
}

export interface Command {
  commandId: string;
  tenantId: string;
  deviceId: string;
  commandType: string;
  payload: string;
  status: string;
  createdAt: string;
  ackedAt?: string;
}

export interface User {
  userId: string;
  tenantId: string;
  username: string;
  email: string;
  role: Role;
  createdAt: string;
}
