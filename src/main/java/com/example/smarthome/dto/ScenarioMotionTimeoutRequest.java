package com.example.smarthome.dto;

public class ScenarioMotionTimeoutRequest {
    private Long deviceId;
    private Long elapsedSeconds;

    public ScenarioMotionTimeoutRequest() {
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(Long elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }
}

