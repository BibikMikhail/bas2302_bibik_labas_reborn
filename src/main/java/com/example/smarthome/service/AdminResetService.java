package com.example.smarthome.service;

import com.example.smarthome.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminResetService {

    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AutomationRuleRepository automationRuleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AdminResetService(RoomRepository roomRepository,
                             DeviceRepository deviceRepository,
                             UserRepository userRepository,
                             EventRepository eventRepository,
                             AutomationRuleRepository automationRuleRepository) {
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.automationRuleRepository = automationRuleRepository;
    }

    @Transactional
    public Map<String, Long> resetAll() {
        long rooms = roomRepository.count();
        long devices = deviceRepository.count();
        long users = userRepository.count();
        long events = eventRepository.count();
        long rules = automationRuleRepository.count();

        // Сбрасываем identity/sequence и очищаем таблицы для "отката" демо.
        // Важно: делаем это только в admin reset, чтобы id не сбрасывались при обычных операциях.
        entityManager.createNativeQuery(
                "TRUNCATE TABLE events, automation_rules, devices, users, rooms RESTART IDENTITY CASCADE"
        ).executeUpdate();

        Map<String, Long> deleted = new HashMap<>();
        deleted.put("rooms", rooms);
        deleted.put("devices", devices);
        deleted.put("users", users);
        deleted.put("events", events);
        deleted.put("automationRules", rules);
        return deleted;
    }
}

