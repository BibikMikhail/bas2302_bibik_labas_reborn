package com.example.smarthome.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HelloController {

    @Value("${spring.application.name:Smart Home API}")
    private String appName;

    @GetMapping("/hello")
    public String hello(@RequestParam(required = false) String name) {
        if (name != null && !name.isBlank()) {
            return "Hello, " + name.trim() + "!";
        }
        return "Hello from " + appName + "!";
    }

    @GetMapping("/")
    public Map<String, Object> info() {
        return Map.of(
                "service", appName,
                "description", "Умный дом: устройства, комнаты, пользователи, правила автоматизации, события",
                "status", "running"
        );
    }
}
