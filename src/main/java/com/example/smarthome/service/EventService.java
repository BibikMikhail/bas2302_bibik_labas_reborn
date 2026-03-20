package com.example.smarthome.service;

import com.example.smarthome.dto.EventLogItem;
import com.example.smarthome.model.AutomationRule;
import com.example.smarthome.model.Event;
import com.example.smarthome.repository.AutomationRuleRepository;
import com.example.smarthome.repository.DeviceRepository;
import com.example.smarthome.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final DeviceRepository deviceRepository;
    private final AutomationRuleRepository automationRuleRepository;

    public EventService(EventRepository eventRepository, DeviceRepository deviceRepository,
                        AutomationRuleRepository automationRuleRepository) {
        this.eventRepository = eventRepository;
        this.deviceRepository = deviceRepository;
        this.automationRuleRepository = automationRuleRepository;
    }

    @Transactional
    public Event create(Event event) {
        if (event.getDeviceId() == null || !deviceRepository.existsById(event.getDeviceId())) {
            throw new IllegalArgumentException("Device with id " + event.getDeviceId() + " not found");
        }
        Event entity = new Event();
        entity.setEventType(event.getEventType());
        entity.setPayload(event.getPayload());
        entity.setCreatedAt(event.getCreatedAt() != null ? event.getCreatedAt() : Instant.now());
        entity.setDevice(deviceRepository.getReferenceById(event.getDeviceId()));
        return eventRepository.save(entity);
    }

    public Optional<Event> getById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> getAll() {
        return eventRepository.findAll();
    }

    public List<Event> getByDeviceId(Long deviceId) {
        return eventRepository.findByDevice_Id(deviceId);
    }

    public List<EventLogItem> getEventsLog() {
        return eventRepository.findAllForLog().stream()
                .map(e -> new EventLogItem(
                        e.getId(),
                        e.getDevice() != null ? e.getDevice().getName() : null,
                        (e.getDevice() != null && e.getDevice().getRoom() != null) ? e.getDevice().getRoom().getName() : null,
                        e.getEventType(),
                        e.getActionPerformed(),
                        e.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public Optional<Event> update(Long id, Event event) {
        if (event.getDeviceId() != null && !deviceRepository.existsById(event.getDeviceId())) {
            throw new IllegalArgumentException("Device with id " + event.getDeviceId() + " not found");
        }
        return eventRepository.findById(id)
                .map(existing -> {
                    existing.setEventType(event.getEventType());
                    existing.setPayload(event.getPayload());
                    if (event.getDeviceId() != null) {
                        existing.setDevice(deviceRepository.getReferenceById(event.getDeviceId()));
                    }
                    if (event.getCreatedAt() != null) existing.setCreatedAt(event.getCreatedAt());
                    return eventRepository.save(existing);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!eventRepository.existsById(id)) return false;
        eventRepository.deleteById(id);
        return true;
    }

    /** Бизнес-операция: записать событие и вернуть подходящие правила (Event + AutomationRule, одна транзакция). */
    @Transactional
    public Map<String, Object> recordEventAndFindMatchingRules(Event event) {
        if (event.getDeviceId() == null || !deviceRepository.existsById(event.getDeviceId())) {
            throw new IllegalArgumentException("Device with id " + event.getDeviceId() + " not found");
        }
        Event entity = new Event();
        entity.setEventType(event.getEventType());
        entity.setPayload(event.getPayload());
        entity.setCreatedAt(event.getCreatedAt() != null ? event.getCreatedAt() : Instant.now());
        entity.setDevice(deviceRepository.getReferenceById(event.getDeviceId()));
        Event saved = eventRepository.save(entity);
        String eventType = saved.getEventType();
        List<AutomationRule> matching = eventType != null
                ? automationRuleRepository.findByTriggerEventTypeAndActiveTrue(eventType)
                : List.of();
        Map<String, Object> result = new HashMap<>();
        result.put("event", saved);
        result.put("matchingRules", matching);
        return result;
    }
}
