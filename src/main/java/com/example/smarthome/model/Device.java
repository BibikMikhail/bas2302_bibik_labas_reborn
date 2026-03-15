package com.example.smarthome.model;

public class Device {
    private Long id;
    private String name;
    private String type;       // например: LAMP, SENSOR, THERMOSTAT
    private Long roomId;
    private boolean active = true;

    public Device() {
    }

    public Device(Long id, String name, String type, Long roomId, boolean active) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.roomId = roomId;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
