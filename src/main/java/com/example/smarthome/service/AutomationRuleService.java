package com.example.smarthome.service;

import com.example.smarthome.model.AutomationRule;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AutomationRuleService {

    private final Map<Long, AutomationRule> storage = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1L);

    private final DeviceService deviceService;
    private final UserService userService;
    private final RoomService roomService;

    public AutomationRuleService(DeviceService deviceService, UserService userService, RoomService roomService) {
        this.deviceService = deviceService;
        this.userService = userService;
        this.roomService = roomService;
    }

    public AutomationRule create(AutomationRule rule) {
        if (rule.getTargetDeviceId() != null && deviceService.getById(rule.getTargetDeviceId()).isEmpty()) {
            throw new IllegalArgumentException("Target device with id " + rule.getTargetDeviceId() + " not found");
        }
        if (rule.getTargetUserId() != null && userService.getById(rule.getTargetUserId()).isEmpty()) {
            throw new IllegalArgumentException("Target user with id " + rule.getTargetUserId() + " not found");
        }
        if (rule.getRoomId() != null && !roomService.exists(rule.getRoomId())) {
            throw new IllegalArgumentException("Room with id " + rule.getRoomId() + " not found");
        }
        AutomationRule entity = new AutomationRule();
        entity.setId(nextId.getAndIncrement());
        entity.setName(rule.getName());
        entity.setTriggerEventType(rule.getTriggerEventType());
        entity.setActionType(rule.getActionType());
        entity.setTargetDeviceId(rule.getTargetDeviceId());
        entity.setTargetUserId(rule.getTargetUserId());
        entity.setRoomId(rule.getRoomId());
        entity.setActive(rule.isActive());
        storage.put(entity.getId(), entity);
        return entity;
    }

    public Optional<AutomationRule> getById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<AutomationRule> getAll() {
        return List.copyOf(storage.values());
    }

    public Optional<AutomationRule> update(Long id, AutomationRule rule) {
        AutomationRule existing = storage.get(id);
        if (existing == null) return Optional.empty();
        if (rule.getTargetDeviceId() != null && deviceService.getById(rule.getTargetDeviceId()).isEmpty()) {
            throw new IllegalArgumentException("Target device with id " + rule.getTargetDeviceId() + " not found");
        }
        if (rule.getTargetUserId() != null && userService.getById(rule.getTargetUserId()).isEmpty()) {
            throw new IllegalArgumentException("Target user with id " + rule.getTargetUserId() + " not found");
        }
        if (rule.getRoomId() != null && !roomService.exists(rule.getRoomId())) {
            throw new IllegalArgumentException("Room with id " + rule.getRoomId() + " not found");
        }
        existing.setName(rule.getName());
        existing.setTriggerEventType(rule.getTriggerEventType());
        existing.setActionType(rule.getActionType());
        existing.setTargetDeviceId(rule.getTargetDeviceId());
        existing.setTargetUserId(rule.getTargetUserId());
        existing.setRoomId(rule.getRoomId());
        existing.setActive(rule.isActive());
        return Optional.of(existing);
    }

    public boolean delete(Long id) {
        return storage.remove(id) != null;
    }

    public boolean hasRulesForRoom(Long roomId) {
        return storage.values().stream()
                .anyMatch(r -> roomId.equals(r.getRoomId()));
    }
}
