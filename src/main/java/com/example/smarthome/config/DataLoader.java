package com.example.smarthome.config;

import com.example.smarthome.model.*;
import com.example.smarthome.repository.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    public ApplicationRunner loadData(RoomRepository roomRepository,
                                      DeviceRepository deviceRepository,
                                      UserRepository userRepository,
                                      EventRepository eventRepository,
                                      AutomationRuleRepository automationRuleRepository) {
        return args -> {
            if (roomRepository.count() > 0) return;

            // Комнаты
            Room kitchen = roomRepository.save(new Room(null, "Кухня", "Кухня"));
            Room bathroom = roomRepository.save(new Room(null, "Санузел", "Санузел"));
            Room living = roomRepository.save(new Room(null, "Зал", "Гостиная"));

            // Устройства (активность ламп по умолчанию выключена для демо)
            Device lampKitchen = deviceRepository.save(makeDevice(deviceRepository, kitchen, "Лампочка кухня", "LAMP", false));
            Device lampBathroom = deviceRepository.save(makeDevice(deviceRepository, bathroom, "Лампочка санузел", "LAMP", false));
            Device lampLiving = deviceRepository.save(makeDevice(deviceRepository, living, "Лампочка зал", "LAMP", false));

            Device motionSensorBathroom = deviceRepository.save(makeDevice(deviceRepository, bathroom, "Датчик движения санузел", "MOTION_SENSOR", true));
            Device leakSensorBathroom = deviceRepository.save(makeDevice(deviceRepository, bathroom, "Датчик протечки санузел", "LEAK_SENSOR", true));
            Device speakerLiving = deviceRepository.save(makeDevice(deviceRepository, living, "Умная колонка зал", "SPEAKER", true));

            // Пользователь для уведомлений
            User user = userRepository.save(new User(null, "Владелец", "owner@home.local"));

            // 1) Движение -> включить свет в санузле
            automationRuleRepository.save(makeRule(
                    "Свет при движении",
                    "MOTION_DETECTED",
                    "TURN_ON_DEVICE",
                    lampBathroom,
                    null,
                    bathroom
            ));

            // 2) Нет движения 30 сек -> выключить свет в санузле
            automationRuleRepository.save(makeRule(
                    "Свет при таймауте движения",
                    "MOTION_TIMEOUT",
                    "TURN_OFF_DEVICE",
                    lampBathroom,
                    null,
                    bathroom
            ));

            // 3) Протечка -> уведомление пользователю
            automationRuleRepository.save(makeRule(
                    "Уведомить о протечке",
                    "LEAK_DETECTED",
                    "NOTIFY_USER",
                    null,
                    user,
                    bathroom
            ));

            // 4) Команда включить/выключить свет в выбранной комнате (колонка)
            // Кухня
            automationRuleRepository.save(makeRule(
                    "Колонка: включить свет (Кухня)",
                    "CMD_TURN_ON_LIGHT",
                    "TURN_ON_DEVICE",
                    lampKitchen,
                    null,
                    kitchen
            ));
            automationRuleRepository.save(makeRule(
                    "Колонка: выключить свет (Кухня)",
                    "CMD_TURN_OFF_LIGHT",
                    "TURN_OFF_DEVICE",
                    lampKitchen,
                    null,
                    kitchen
            ));

            // Санузел
            automationRuleRepository.save(makeRule(
                    "Колонка: включить свет (Санузел)",
                    "CMD_TURN_ON_LIGHT",
                    "TURN_ON_DEVICE",
                    lampBathroom,
                    null,
                    bathroom
            ));
            automationRuleRepository.save(makeRule(
                    "Колонка: выключить свет (Санузел)",
                    "CMD_TURN_OFF_LIGHT",
                    "TURN_OFF_DEVICE",
                    lampBathroom,
                    null,
                    bathroom
            ));

            // Зал
            automationRuleRepository.save(makeRule(
                    "Колонка: включить свет (Зал)",
                    "CMD_TURN_ON_LIGHT",
                    "TURN_ON_DEVICE",
                    lampLiving,
                    null,
                    living
            ));
            automationRuleRepository.save(makeRule(
                    "Колонка: выключить свет (Зал)",
                    "CMD_TURN_OFF_LIGHT",
                    "TURN_OFF_DEVICE",
                    lampLiving,
                    null,
                    living
            ));
        };
    }

    private Device makeDevice(DeviceRepository deviceRepository, Room room, String name, String type, boolean active) {
        Device d = new Device();
        d.setName(name);
        d.setType(type);
        d.setRoom(room);
        d.setActive(active);
        return d;
    }

    private AutomationRule makeRule(String ruleName,
                                    String triggerEventType,
                                    String actionType,
                                    Device targetDevice,
                                    User targetUser,
                                    Room room) {
        AutomationRule rule = new AutomationRule();
        rule.setName(ruleName);
        rule.setTriggerEventType(triggerEventType);
        rule.setActionType(actionType);
        rule.setTargetDevice(targetDevice);
        rule.setTargetUser(targetUser);
        rule.setRoom(room);
        rule.setActive(true);
        return rule;
    }
}
