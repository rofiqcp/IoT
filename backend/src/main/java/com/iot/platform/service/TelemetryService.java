package com.iot.platform.service;

import com.iot.platform.model.TelemetryRecord;
import com.iot.platform.repository.TelemetryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;

    public TelemetryService(TelemetryRepository telemetryRepository) {
        this.telemetryRepository = telemetryRepository;
    }

    public void save(UUID tenantId, UUID deviceId, String metricName,
                     Double metricValue, String unit, Instant recordedAt) {
        String dayBucket = LocalDate.ofInstant(recordedAt, ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
        TelemetryRecord record = new TelemetryRecord(
                tenantId, deviceId, dayBucket, recordedAt, metricName, metricValue, unit);
        telemetryRepository.save(record);
    }

    public List<TelemetryRecord> getByDeviceAndDate(UUID tenantId, UUID deviceId,
                                                     String date, int limit) {
        return telemetryRepository.findByDeviceAndDay(tenantId, deviceId, date, limit);
    }

    public List<TelemetryRecord> getLatest(UUID tenantId, UUID deviceId, int limit) {
        String today = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
        return telemetryRepository.findByDeviceAndDay(tenantId, deviceId, today, limit);
    }
}
