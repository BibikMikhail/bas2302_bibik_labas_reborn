package com.example.smarthome.model;

public class AutomationRule {
    private Long id;
    private String name;
    private String triggerEventType;  // на какой тип события реагируем
    private String actionType;        // TURN_ON_DEVICE, TURN_OFF_DEVICE, NOTIFY_USER
    private Long targetDeviceId;      // для TURN_ON_DEVICE / TURN_OFF_DEVICE
    private Long targetUserId;        // для NOTIFY_USER
    private Long roomId;              // привязка к комнате (правило для комнаты)
    private boolean active = true;

    public AutomationRule() {
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

    public String getTriggerEventType() {
        return triggerEventType;
    }

    public void setTriggerEventType(String triggerEventType) {
        this.triggerEventType = triggerEventType;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Long getTargetDeviceId() {
        return targetDeviceId;
    }

    public void setTargetDeviceId(Long targetDeviceId) {
        this.targetDeviceId = targetDeviceId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
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
