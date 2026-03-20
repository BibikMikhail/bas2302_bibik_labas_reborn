package com.example.smarthome.service;

import com.example.smarthome.model.Device;
import com.example.smarthome.model.Room;
import com.example.smarthome.repository.AutomationRuleRepository;
import com.example.smarthome.repository.DeviceRepository;
import com.example.smarthome.repository.EventRepository;
import com.example.smarthome.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final AutomationRuleRepository automationRuleRepository;
    private final EventRepository eventRepository;

    public RoomService(RoomRepository roomRepository,
                       DeviceRepository deviceRepository,
                       AutomationRuleRepository automationRuleRepository,
                       EventRepository eventRepository) {
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
        this.automationRuleRepository = automationRuleRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Room create(Room room) {
        Room entity = new Room(null, room.getName(), room.getDescription());
        return roomRepository.save(entity);
    }

    public Optional<Room> getById(Long id) {
        return roomRepository.findById(id);
    }

    public List<Room> getAll() {
        return roomRepository.findAll();
    }

    @Transactional
    public Optional<Room> update(Long id, Room room) {
        return roomRepository.findById(id)
                .map(existing -> {
                    existing.setName(room.getName());
                    existing.setDescription(room.getDescription());
                    return roomRepository.save(existing);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!roomRepository.existsById(id)) return false;
        if (!deviceRepository.findByRoom_Id(id).isEmpty()) return false;
        if (!automationRuleRepository.findByRoom_Id(id).isEmpty()) return false;
        roomRepository.deleteById(id);
        return true;
    }

    public boolean exists(Long id) {
        return roomRepository.existsById(id);
    }

    /** Бизнес-операция: создать комнату и первое устройство в ней (одна транзакция). */
    @Transactional
    public Map<String, Object> createRoomWithDevice(Room room, Device device) {
        Room savedRoom = roomRepository.save(new Room(null, room.getName(), room.getDescription()));
        Device deviceEntity = new Device();
        deviceEntity.setName(device.getName());
        deviceEntity.setType(device.getType());
        deviceEntity.setActive(device.isActive());
        deviceEntity.setRoom(savedRoom);
        Device savedDevice = deviceRepository.save(deviceEntity);
        return Map.of("room", savedRoom, "device", savedDevice);
    }

    /** Бизнес-операция: комната со списком устройств и правил (Room + Device + AutomationRule). */
    public Map<String, Object> getRoomWithDevicesAndRules(Long roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) return null;
        List<Device> devices = deviceRepository.findByRoom_Id(roomId);
        List<com.example.smarthome.model.AutomationRule> rules = automationRuleRepository.findByRoom_Id(roomId);
        return Map.of("room", room, "devices", devices, "automationRules", rules);
    }

    /** Бизнес-операция: все события по комнате (устройства в комнате → их события). */
    public List<com.example.smarthome.model.Event> getEventsForRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) return List.of();
        return eventRepository.findByDevice_Room_IdOrderByCreatedAtDesc(roomId);
    }
}
