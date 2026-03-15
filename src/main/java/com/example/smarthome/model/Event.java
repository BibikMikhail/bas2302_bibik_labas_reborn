package com.example.smarthome.model;

import java.time.Instant;

public class Event {
    private Long id;
    private Long deviceId;
    private String eventType;   // например: MOTION_DETECTED, TEMPERATURE_HIGH, BUTTON_PRESSED
    private String payload;     // произвольные данные (JSON или текст)
    private Instant createdAt;

    public Event() {
    }

    public Event(Long id, Long deviceId, String eventType, String payload, Instant createdAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
