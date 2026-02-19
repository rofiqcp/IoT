import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import type { TelemetryRecord } from '../types';

interface Props {
  records: TelemetryRecord[];
}

const COLORS = ['#8884d8', '#82ca9d', '#ffc658', '#ff7300', '#00C49F'];

const TelemetryChart: React.FC<Props> = ({ records }) => {
  // Group by timestamp, pivot metric names into columns
  const metricNames = [...new Set(records.map((r) => r.metricName))];

  const grouped: Record<string, Record<string, number>> = {};
  for (const r of records) {
    const key = r.recordedAt;
    if (!grouped[key]) grouped[key] = { time: new Date(r.recordedAt).getTime() } as any;
    grouped[key][r.metricName] = r.metricValue;
  }

  const chartData = Object.values(grouped)
    .sort((a: any, b: any) => a.time - b.time)
    .slice(-50); // last 50 data points

  return (
    <div style={{ width: '100%', height: 300 }}>
      <ResponsiveContainer>
        <LineChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis
            dataKey="time"
            type="number"
            domain={['dataMin', 'dataMax']}
            tickFormatter={(v) => new Date(v).toLocaleTimeString()}
          />
          <YAxis />
          <Tooltip labelFormatter={(v) => new Date(v as number).toLocaleString()} />
          <Legend />
          {metricNames.map((name, i) => (
            <Line
              key={name}
              type="monotone"
              dataKey={name}
              stroke={COLORS[i % COLORS.length]}
              dot={false}
              strokeWidth={2}
            />
          ))}
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

export default TelemetryChart;
