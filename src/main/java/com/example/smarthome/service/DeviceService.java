package com.example.smarthome.service;

import com.example.smarthome.model.Device;
import com.example.smarthome.model.Event;
import com.example.smarthome.model.Room;
import com.example.smarthome.repository.DeviceRepository;
import com.example.smarthome.repository.EventRepository;
import com.example.smarthome.repository.RoomRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    private final EventRepository eventRepository;

    public DeviceService(DeviceRepository deviceRepository, RoomRepository roomRepository, EventRepository eventRepository) {
        this.deviceRepository = deviceRepository;
        this.roomRepository = roomRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Device create(Device device) {
        if (device.getRoomId() != null && !roomRepository.existsById(device.getRoomId())) {
            throw new IllegalArgumentException("Room with id " + device.getRoomId() + " not found");
        }
        Device entity = new Device();
        entity.setName(device.getName());
        entity.setType(device.getType());
        entity.setActive(device.isActive());
        if (device.getRoomId() != null) {
            entity.setRoom(roomRepository.getReferenceById(device.getRoomId()));
        }
        return deviceRepository.save(entity);
    }

    public Optional<Device> getById(Long id) {
        return deviceRepository.findById(id);
    }

    public List<Device> getAll() {
        return deviceRepository.findAll();
    }

    public List<Device> getByRoomId(Long roomId) {
        return deviceRepository.findByRoom_Id(roomId);
    }

    @Transactional
    public Optional<Device> update(Long id, Device device) {
        if (device.getRoomId() != null && !roomRepository.existsById(device.getRoomId())) {
            throw new IllegalArgumentException("Room with id " + device.getRoomId() + " not found");
        }
        return deviceRepository.findById(id)
                .map(existing -> {
                    existing.setName(device.getName());
                    existing.setType(device.getType());
                    existing.setActive(device.isActive());
                    if (device.getRoomId() != null) {
                        existing.setRoom(roomRepository.getReferenceById(device.getRoomId()));
                    } else {
                        existing.setRoom(null);
                    }
                    return deviceRepository.save(existing);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!deviceRepository.existsById(id)) return false;
        deviceRepository.deleteById(id);
        return true;
    }

    public Map<String, Object> getDeviceWithLastEvent(Long deviceId) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) return null;
        List<Event> lastEvents = eventRepository.findByDevice_IdOrderByCreatedAtDesc(deviceId, PageRequest.of(0, 1));
        Map<String, Object> result = new HashMap<>();
        result.put("device", deviceOpt.get());
        result.put("lastEvent", lastEvents.isEmpty() ? null : lastEvents.get(0));
        return result;
    }
}
