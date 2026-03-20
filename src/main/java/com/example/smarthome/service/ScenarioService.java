package com.example.smarthome.service;

import com.example.smarthome.dto.*;
import com.example.smarthome.model.*;
import com.example.smarthome.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScenarioService {

    private static final String TYPE_LAMP = "LAMP";
    private static final String TYPE_MOTION_SENSOR = "MOTION_SENSOR";
    private static final String TYPE_LEAK_SENSOR = "LEAK_SENSOR";
    private static final String TYPE_SPEAKER = "SPEAKER";

    private static final String EVENT_MOTION_DETECTED = "MOTION_DETECTED";
    private static final String EVENT_MOTION_TIMEOUT = "MOTION_TIMEOUT";
    private static final String EVENT_LEAK_DETECTED = "LEAK_DETECTED";
    private static final String EVENT_CMD_TURN_ON = "CMD_TURN_ON_LIGHT";
    private static final String EVENT_CMD_TURN_OFF = "CMD_TURN_OFF_LIGHT";

    private static final String ACTION_TURN_ON_DEVICE = "TURN_ON_DEVICE";
    private static final String ACTION_TURN_OFF_DEVICE = "TURN_OFF_DEVICE";
    private static final String ACTION_NOTIFY_USER = "NOTIFY_USER";

    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final AutomationRuleRepository automationRuleRepository;
    private final EventRepository eventRepository;

    public ScenarioService(RoomRepository roomRepository,
                           DeviceRepository deviceRepository,
                           UserRepository userRepository,
                           AutomationRuleRepository automationRuleRepository,
                           EventRepository eventRepository) {
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.automationRuleRepository = automationRuleRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Map<String, Object> motionDetected(ScenarioDeviceRequest request) {
        return motionDetectedInternal(request.getDeviceId());
    }

    @Transactional
    public Map<String, Object> motionTimeout(ScenarioMotionTimeoutRequest request) {
        return motionTimeoutInternal(request.getDeviceId(), request.getElapsedSeconds());
    }

    @Transactional
    public Map<String, Object> leakDetected(ScenarioDeviceRequest request) {
        return leakDetectedInternal(request.getDeviceId());
    }

    @Transactional
    public Map<String, Object> speakerTurnOn(ScenarioSpeakerCommandRequest request) {
        return speakerCommandInternal(request.getDeviceId(), request.getRoomName(), true);
    }

    @Transactional
    public Map<String, Object> speakerTurnOff(ScenarioSpeakerCommandRequest request) {
        return speakerCommandInternal(request.getDeviceId(), request.getRoomName(), false);
    }

    private Map<String, Object> motionDetectedInternal(Long deviceId) {
        Device sensor = requireDevice(deviceId, TYPE_MOTION_SENSOR, "Датчик движения");
        Long roomId = sensor.getRoom().getId();

        List<AutomationRule> rules = automationRuleRepository
                .findByTriggerEventTypeAndRoom_IdAndActiveTrue(EVENT_MOTION_DETECTED, roomId);

        List<String> performed = new ArrayList<>();
        for (AutomationRule rule : rules) {
            if (ACTION_TURN_ON_DEVICE.equals(rule.getActionType())) {
                Device target = resolveTargetDevice(rule);
                target.setActive(true);
                deviceRepository.save(target);
                performed.add("Включил свет в комнате " + target.getRoom().getName());
            }
        }

        String actionPerformed = performed.isEmpty() ? "Правила не сработали" : String.join("; ", performed);
        Event event = newEvent(sensor.getId(), EVENT_MOTION_DETECTED, null, actionPerformed);
        Event saved = eventRepository.save(event);

        Map<String, Object> result = new HashMap<>();
        result.put("eventId", saved.getId());
        result.put("actionPerformed", actionPerformed);
        return result;
    }

    private Map<String, Object> motionTimeoutInternal(Long deviceId, Long elapsedSeconds) {
        if (elapsedSeconds == null) throw new IllegalArgumentException("elapsedSeconds is required");
        Device sensor = requireDevice(deviceId, TYPE_MOTION_SENSOR, "Датчик движения");
        Long roomId = sensor.getRoom().getId();

        List<AutomationRule> rules = automationRuleRepository
                .findByTriggerEventTypeAndRoom_IdAndActiveTrue(EVENT_MOTION_TIMEOUT, roomId);

        List<String> performed = new ArrayList<>();
        if (elapsedSeconds >= 30) {
            for (AutomationRule rule : rules) {
                if (ACTION_TURN_OFF_DEVICE.equals(rule.getActionType())) {
                    Device target = resolveTargetDevice(rule);
                    target.setActive(false);
                    deviceRepository.save(target);
                    performed.add("Выключил свет в комнате " + target.getRoom().getName());
                }
            }
        }

        String actionPerformed;
        if (performed.isEmpty()) {
            actionPerformed = elapsedSeconds >= 30
                    ? "Правила не нашли действие"
                    : "Прошло " + elapsedSeconds + " сек, выключение не выполнено";
        } else {
            actionPerformed = String.join("; ", performed);
        }

        Event event = newEvent(sensor.getId(), EVENT_MOTION_TIMEOUT, String.valueOf(elapsedSeconds), actionPerformed);
        Event saved = eventRepository.save(event);

        Map<String, Object> result = new HashMap<>();
        result.put("eventId", saved.getId());
        result.put("actionPerformed", actionPerformed);
        return result;
    }

    private Map<String, Object> leakDetectedInternal(Long deviceId) {
        Device sensor = requireDevice(deviceId, TYPE_LEAK_SENSOR, "Датчик протечки");
        Long roomId = sensor.getRoom().getId();

        List<AutomationRule> rules = automationRuleRepository
                .findByTriggerEventTypeAndRoom_IdAndActiveTrue(EVENT_LEAK_DETECTED, roomId);

        String roomName = sensor.getRoom().getName();
        List<String> performed = new ArrayList<>();
        String payload = null;
        boolean isAdmin = currentIsAdmin();
        for (AutomationRule rule : rules) {
            if (ACTION_NOTIFY_USER.equals(rule.getActionType())) {
                User user = resolveTargetUser(rule);
                String message = "Протечка обнаружена в комнате \"" + roomName + "\"";
                if (isAdmin) {
                    payload = message;
                    performed.add("Уведомление пользователю " + user.getName());
                } else {
                    performed.add("Уведомление отправлено администратору");
                }
            }
        }

        String actionPerformed = performed.isEmpty() ? "Правила не сработали" : String.join("; ", performed);
        Event event = newEvent(sensor.getId(), EVENT_LEAK_DETECTED, payload, actionPerformed);
        Event saved = eventRepository.save(event);

        Map<String, Object> result = new HashMap<>();
        result.put("eventId", saved.getId());
        result.put("actionPerformed", actionPerformed);
        return result;
    }

    private boolean currentIsAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) return true;
        }
        return false;
    }

    private Map<String, Object> speakerCommandInternal(Long speakerDeviceId, String roomName, boolean turnOn) {
        if (roomName == null || roomName.isBlank()) {
            throw new IllegalArgumentException("roomName is required");
        }
        Device speaker = requireDevice(speakerDeviceId, TYPE_SPEAKER, "Умная колонка");
        Room targetRoom = roomRepository.findByName(roomName)
                .orElseThrow(() -> new IllegalArgumentException("Room with name '" + roomName + "' not found"));

        String eventType = turnOn ? EVENT_CMD_TURN_ON : EVENT_CMD_TURN_OFF;
        List<AutomationRule> rules = automationRuleRepository
                .findByTriggerEventTypeAndRoom_IdAndActiveTrue(eventType, targetRoom.getId());

        List<String> performed = new ArrayList<>();
        for (AutomationRule rule : rules) {
            if (turnOn && ACTION_TURN_ON_DEVICE.equals(rule.getActionType())) {
                Device target = resolveTargetDevice(rule);
                target.setActive(true);
                deviceRepository.save(target);
                performed.add("Включил свет в комнате " + targetRoom.getName());
            }
            if (!turnOn && ACTION_TURN_OFF_DEVICE.equals(rule.getActionType())) {
                Device target = resolveTargetDevice(rule);
                target.setActive(false);
                deviceRepository.save(target);
                performed.add("Выключил свет в комнате " + targetRoom.getName());
            }
        }

        String actionPerformed = performed.isEmpty() ? "Правила не сработали" : String.join("; ", performed);
        Event event = newEvent(speaker.getId(), eventType, "roomName=" + roomName, actionPerformed);
        Event saved = eventRepository.save(event);

        Map<String, Object> result = new HashMap<>();
        result.put("eventId", saved.getId());
        result.put("actionPerformed", actionPerformed);
        return result;
    }

    private Device requireDevice(Long deviceId, String expectedType, String expectedName) {
        if (deviceId == null) throw new IllegalArgumentException("deviceId is required");
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device with id " + deviceId + " not found"));
        if (device.getType() == null || !device.getType().equalsIgnoreCase(expectedType)) {
            throw new IllegalArgumentException(expectedName + " expected, but got type '" + device.getType() + "'");
        }
        if (device.getRoom() == null) {
            throw new IllegalArgumentException("Device must belong to a room");
        }
        return device;
    }

    private Device resolveTargetDevice(AutomationRule rule) {
        Device target = rule.getTargetDevice();
        if (target != null) return target;
        Long targetId = rule.getTargetDeviceId();
        if (targetId == null) {
            throw new IllegalArgumentException("Rule has no targetDevice");
        }
        return deviceRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Target device with id " + targetId + " not found"));
    }

    private User resolveTargetUser(AutomationRule rule) {
        User user = rule.getTargetUser();
        if (user != null) return user;
        Long userId = rule.getTargetUserId();
        if (userId == null) {
            throw new IllegalArgumentException("Rule has no targetUser");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Target user with id " + userId + " not found"));
    }

    private Event newEvent(Long deviceId, String eventType, String payload, String actionPerformed) {
        Event ev = new Event();
        ev.setEventType(eventType);
        ev.setPayload(payload);
        ev.setCreatedAt(Instant.now());
        ev.setDevice(deviceRepository.getReferenceById(deviceId));
        ev.setActionPerformed(actionPerformed);
        return ev;
    }
}

