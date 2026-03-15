package com.example.smarthome.service;

import com.example.smarthome.model.Room;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RoomService {

    private final Map<Long, Room> storage = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1L);

    private final DeviceService deviceService;
    private final AutomationRuleService automationRuleService;

    public RoomService(@Lazy DeviceService deviceService, @Lazy AutomationRuleService automationRuleService) {
        this.deviceService = deviceService;
        this.automationRuleService = automationRuleService;
    }

    public Room create(Room room) {
        Room entity = new Room(null, room.getName(), room.getDescription());
        entity.setId(nextId.getAndIncrement());
        storage.put(entity.getId(), entity);
        return entity;
    }

    public Optional<Room> getById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Room> getAll() {
        return List.copyOf(storage.values());
    }

    public Optional<Room> update(Long id, Room room) {
        Room existing = storage.get(id);
        if (existing == null) return Optional.empty();
        existing.setName(room.getName());
        existing.setDescription(room.getDescription());
        return Optional.of(existing);
    }

    public boolean delete(Long id) {
        if (!storage.containsKey(id)) return false;
        if (deviceService.hasDevicesInRoom(id)) return false;
        if (automationRuleService.hasRulesForRoom(id)) return false;
        storage.remove(id);
        return true;
    }

    public boolean exists(Long id) {
        return storage.containsKey(id);
    }
}
