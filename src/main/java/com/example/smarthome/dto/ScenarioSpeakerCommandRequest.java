package com.example.smarthome.dto;

public class ScenarioSpeakerCommandRequest {
    private Long deviceId;
    private String roomName;

    public ScenarioSpeakerCommandRequest() {
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}

