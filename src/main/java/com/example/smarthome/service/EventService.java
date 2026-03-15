package com.example.smarthome.service;

import com.example.smarthome.model.Event;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EventService {

    private final Map<Long, Event> storage = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1L);

    private final DeviceService deviceService;

    public EventService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public Event create(Event event) {
        if (event.getDeviceId() != null && deviceService.getById(event.getDeviceId()).isEmpty()) {
            throw new IllegalArgumentException("Device with id " + event.getDeviceId() + " not found");
        }
        Event entity = new Event(
                null,
                event.getDeviceId(),
                event.getEventType(),
                event.getPayload(),
                event.getCreatedAt() != null ? event.getCreatedAt() : Instant.now()
        );
        entity.setId(nextId.getAndIncrement());
        storage.put(entity.getId(), entity);
        return entity;
    }

    public Optional<Event> getById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Event> getAll() {
        return List.copyOf(storage.values());
    }

    public List<Event> getByDeviceId(Long deviceId) {
        return storage.values().stream()
                .filter(e -> deviceId.equals(e.getDeviceId()))
                .toList();
    }

    public Optional<Event> update(Long id, Event event) {
        Event existing = storage.get(id);
        if (existing == null) return Optional.empty();
        if (event.getDeviceId() != null && deviceService.getById(event.getDeviceId()).isEmpty()) {
            throw new IllegalArgumentException("Device with id " + event.getDeviceId() + " not found");
        }
        existing.setDeviceId(event.getDeviceId());
        existing.setEventType(event.getEventType());
        existing.setPayload(event.getPayload());
        if (event.getCreatedAt() != null) existing.setCreatedAt(event.getCreatedAt());
        return Optional.of(existing);
    }

    public boolean delete(Long id) {
        return storage.remove(id) != null;
    }
}
