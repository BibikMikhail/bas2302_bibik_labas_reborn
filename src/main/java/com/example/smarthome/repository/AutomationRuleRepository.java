package com.example.smarthome.repository;

import com.example.smarthome.model.AutomationRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {

    List<AutomationRule> findByRoom_Id(Long roomId);

    List<AutomationRule> findByTriggerEventTypeAndActiveTrue(String eventType);

    List<AutomationRule> findByTriggerEventTypeAndRoom_IdAndActiveTrue(String eventType, Long roomId);
}
