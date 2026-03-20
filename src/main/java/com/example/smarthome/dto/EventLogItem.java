package com.example.smarthome.dto;

import java.time.Instant;

public class EventLogItem {

    private Long id;
    private String deviceName;
    private String roomName;
    private String eventType;
    private String actionPerformed;
    private Instant createdAt;

    public EventLogItem() {
    }

    public EventLogItem(Long id, String deviceName, String roomName, String eventType, String actionPerformed, Instant createdAt) {
        this.id = id;
        this.deviceName = deviceName;
        this.roomName = roomName;
        this.eventType = eventType;
        this.actionPerformed = actionPerformed;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getActionPerformed() {
        return actionPerformed;
    }

    public void setActionPerformed(String actionPerformed) {
        this.actionPerformed = actionPerformed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

