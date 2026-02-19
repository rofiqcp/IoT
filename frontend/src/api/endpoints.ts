import api from './client';
import type {
  AuthTokens, Site, Device, DeviceState,
  TelemetryRecord, Alarm, Command, User, Tenant
} from '../types';

// --- Auth ---
export const login = (username: string, password: string) =>
  api.post<AuthTokens>('/auth/login', { username, password });

export const register = (data: {
  tenantId: string; username: string; email: string; password: string; role?: string;
}) => api.post('/auth/register', data);

// --- Tenants ---
export const getTenants = () => api.get<Tenant[]>('/tenants');
export const createTenant = (name: string) => api.post<Tenant>('/tenants', { name });
export const getMyTenant = () => api.get<Tenant>('/tenants/me');

// --- Sites ---
export const getSites = () => api.get<Site[]>('/sites');
export const createSite = (name: string, location: string) =>
  api.post<Site>('/sites', { name, location });

// --- Devices ---
export const getDevices = (siteId: string) =>
  api.get<Device[]>('/devices', { params: { siteId } });
export const getDevice = (deviceId: string, siteId: string) =>
  api.get<Device>(`/devices/${deviceId}`, { params: { siteId } });
export const createDevice = (siteId: string, name: string, deviceType: string) =>
  api.post<Device>('/devices', { siteId, name, deviceType });
export const getDeviceStates = () => api.get<DeviceState[]>('/devices/states');

// --- Telemetry ---
export const getTelemetry = (deviceId: string, date?: string, limit = 100) =>
  api.get<TelemetryRecord[]>(`/telemetry/${deviceId}`, { params: { date, limit } });

// --- Alarms ---
export const getAlarms = (limit = 100) =>
  api.get<Alarm[]>('/alarms', { params: { limit } });
export const acknowledgeAlarm = (alarmId: string) =>
  api.post<Alarm>(`/alarms/${alarmId}/acknowledge`);
export const getAlarmRules = () => api.get('/alarms/rules');
export const createAlarmRule = (data: {
  metricName: string; operator: string; threshold: number; severity: string;
}) => api.post('/alarms/rules', data);

// --- Commands ---
export const sendCommand = (deviceId: string, commandType: string, payload = '{}') =>
  api.post<Command>('/commands', { deviceId, commandType, payload });
export const getCommands = (deviceId: string, limit = 50) =>
  api.get<Command[]>(`/commands/${deviceId}`, { params: { limit } });

// --- Users ---
export const getUsers = () => api.get<User[]>('/users');
export const createUser = (data: {
  username: string; email: string; password: string; role: string;
}) => api.post('/users', data);
export const deleteUser = (userId: string) => api.delete(`/users/${userId}`);
