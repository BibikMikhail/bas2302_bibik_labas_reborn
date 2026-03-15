package com.example.smarthome.service;

import com.example.smarthome.model.Device;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DeviceService {

    private final Map<Long, Device> storage = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1L);

    private final RoomService roomService;

    public DeviceService(RoomService roomService) {
        this.roomService = roomService;
    }

    public Device create(Device device) {
        if (device.getRoomId() != null && !roomService.exists(device.getRoomId())) {
            throw new IllegalArgumentException("Room with id " + device.getRoomId() + " not found");
        }
        Device entity = new Device(null, device.getName(), device.getType(), device.getRoomId(), device.isActive());
        entity.setId(nextId.getAndIncrement());
        storage.put(entity.getId(), entity);
        return entity;
    }

    public Optional<Device> getById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Device> getAll() {
        return List.copyOf(storage.values());
    }

    public List<Device> getByRoomId(Long roomId) {
        return storage.values().stream()
                .filter(d -> roomId.equals(d.getRoomId()))
                .toList();
    }

    public Optional<Device> update(Long id, Device device) {
        Device existing = storage.get(id);
        if (existing == null) return Optional.empty();
        if (device.getRoomId() != null && !roomService.exists(device.getRoomId())) {
            throw new IllegalArgumentException("Room with id " + device.getRoomId() + " not found");
        }
        existing.setName(device.getName());
        existing.setType(device.getType());
        existing.setRoomId(device.getRoomId());
        existing.setActive(device.isActive());
        return Optional.of(existing);
    }

    public boolean delete(Long id) {
        return storage.remove(id) != null;
    }

    public boolean hasDevicesInRoom(Long roomId) {
        return storage.values().stream().anyMatch(d -> roomId.equals(d.getRoomId()));
    }
}
