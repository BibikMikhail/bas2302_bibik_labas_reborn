package com.example.smarthome.controller;

import com.example.smarthome.model.AutomationRule;
import com.example.smarthome.service.AutomationRuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/automation-rules")
public class AutomationRuleController {

    private final AutomationRuleService automationRuleService;

    public AutomationRuleController(AutomationRuleService automationRuleService) {
        this.automationRuleService = automationRuleService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AutomationRule rule) {
        try {
            AutomationRule created = automationRuleService.create(rule);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AutomationRule> getById(@PathVariable Long id) {
        return automationRuleService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<AutomationRule> getAll() {
        return automationRuleService.getAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AutomationRule rule) {
        try {
            return automationRuleService.update(id, rule)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!automationRuleService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
