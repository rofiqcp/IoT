import { create } from 'zustand';
import type { Device, DeviceState, TelemetryRecord, Alarm } from '../types';

interface DeviceStore {
  devices: Device[];
  states: Record<string, DeviceState>;
  telemetry: Record<string, TelemetryRecord[]>;
  alarms: Alarm[];
  setDevices: (devices: Device[]) => void;
  updateState: (state: DeviceState) => void;
  setStates: (states: DeviceState[]) => void;
  appendTelemetry: (deviceId: string, record: TelemetryRecord) => void;
  setTelemetry: (deviceId: string, records: TelemetryRecord[]) => void;
  setAlarms: (alarms: Alarm[]) => void;
}

export const useDeviceStore = create<DeviceStore>((set) => ({
  devices: [],
  states: {},
  telemetry: {},
  alarms: [],

  setDevices: (devices) => set({ devices }),

  updateState: (state) =>
    set((s) => ({ states: { ...s.states, [state.deviceId]: state } })),

  setStates: (states) =>
    set({
      states: Object.fromEntries(states.map((s) => [s.deviceId, s])),
    }),

  appendTelemetry: (deviceId, record) =>
    set((s) => ({
      telemetry: {
        ...s.telemetry,
        [deviceId]: [...(s.telemetry[deviceId] || []).slice(-199), record],
      },
    })),

  setTelemetry: (deviceId, records) =>
    set((s) => ({
      telemetry: { ...s.telemetry, [deviceId]: records },
    })),

  setAlarms: (alarms) => set({ alarms }),
}));
