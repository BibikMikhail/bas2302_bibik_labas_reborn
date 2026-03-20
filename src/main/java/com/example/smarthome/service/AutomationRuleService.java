package com.example.smarthome.service;

import com.example.smarthome.model.AutomationRule;
import com.example.smarthome.model.Device;
import com.example.smarthome.model.Room;
import com.example.smarthome.model.User;
import com.example.smarthome.repository.AutomationRuleRepository;
import com.example.smarthome.repository.DeviceRepository;
import com.example.smarthome.repository.RoomRepository;
import com.example.smarthome.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AutomationRuleService {

    private final AutomationRuleRepository automationRuleRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public AutomationRuleService(AutomationRuleRepository automationRuleRepository,
                                 DeviceRepository deviceRepository,
                                 UserRepository userRepository,
                                 RoomRepository roomRepository) {
        this.automationRuleRepository = automationRuleRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public AutomationRule create(AutomationRule rule) {
        if (rule.getTargetDeviceId() != null && !deviceRepository.existsById(rule.getTargetDeviceId())) {
            throw new IllegalArgumentException("Target device with id " + rule.getTargetDeviceId() + " not found");
        }
        if (rule.getTargetUserId() != null && !userRepository.existsById(rule.getTargetUserId())) {
            throw new IllegalArgumentException("Target user with id " + rule.getTargetUserId() + " not found");
        }
        if (rule.getRoomId() != null && !roomRepository.existsById(rule.getRoomId())) {
            throw new IllegalArgumentException("Room with id " + rule.getRoomId() + " not found");
        }
        AutomationRule entity = new AutomationRule();
        entity.setName(rule.getName());
        entity.setTriggerEventType(rule.getTriggerEventType());
        entity.setActionType(rule.getActionType());
        entity.setActive(rule.isActive());
        if (rule.getTargetDeviceId() != null) {
            entity.setTargetDevice(deviceRepository.getReferenceById(rule.getTargetDeviceId()));
        }
        if (rule.getTargetUserId() != null) {
            entity.setTargetUser(userRepository.getReferenceById(rule.getTargetUserId()));
        }
        if (rule.getRoomId() != null) {
            entity.setRoom(roomRepository.getReferenceById(rule.getRoomId()));
        }
        return automationRuleRepository.save(entity);
    }

    public Optional<AutomationRule> getById(Long id) {
        return automationRuleRepository.findById(id);
    }

    public List<AutomationRule> getAll() {
        return automationRuleRepository.findAll();
    }

    @Transactional
    public Optional<AutomationRule> update(Long id, AutomationRule rule) {
        if (rule.getTargetDeviceId() != null && !deviceRepository.existsById(rule.getTargetDeviceId())) {
            throw new IllegalArgumentException("Target device with id " + rule.getTargetDeviceId() + " not found");
        }
        if (rule.getTargetUserId() != null && !userRepository.existsById(rule.getTargetUserId())) {
            throw new IllegalArgumentException("Target user with id " + rule.getTargetUserId() + " not found");
        }
        if (rule.getRoomId() != null && !roomRepository.existsById(rule.getRoomId())) {
            throw new IllegalArgumentException("Room with id " + rule.getRoomId() + " not found");
        }
        return automationRuleRepository.findById(id)
                .map(existing -> {
                    existing.setName(rule.getName());
                    existing.setTriggerEventType(rule.getTriggerEventType());
                    existing.setActionType(rule.getActionType());
                    existing.setActive(rule.isActive());
                    existing.setTargetDevice(rule.getTargetDeviceId() != null ? deviceRepository.getReferenceById(rule.getTargetDeviceId()) : null);
                    existing.setTargetUser(rule.getTargetUserId() != null ? userRepository.getReferenceById(rule.getTargetUserId()) : null);
                    existing.setRoom(rule.getRoomId() != null ? roomRepository.getReferenceById(rule.getRoomId()) : null);
                    return automationRuleRepository.save(existing);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!automationRuleRepository.existsById(id)) return false;
        automationRuleRepository.deleteById(id);
        return true;
    }
}
