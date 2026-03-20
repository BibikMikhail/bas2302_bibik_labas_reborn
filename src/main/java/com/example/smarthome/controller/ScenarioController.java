package com.example.smarthome.controller;

import com.example.smarthome.dto.*;
import com.example.smarthome.service.ScenarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private final ScenarioService scenarioService;

    public ScenarioController(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @PostMapping("/motion-detected")
    public ResponseEntity<?> motionDetected(@RequestBody ScenarioDeviceRequest request) {
        try {
            Map<String, Object> result = scenarioService.motionDetected(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/no-motion-timeout")
    public ResponseEntity<?> noMotionTimeout(@RequestBody ScenarioMotionTimeoutRequest request) {
        try {
            Map<String, Object> result = scenarioService.motionTimeout(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/leak-detected")
    public ResponseEntity<?> leakDetected(@RequestBody ScenarioDeviceRequest request) {
        try {
            Map<String, Object> result = scenarioService.leakDetected(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/speaker-turn-on")
    public ResponseEntity<?> speakerTurnOn(@RequestBody ScenarioSpeakerCommandRequest request) {
        try {
            Map<String, Object> result = scenarioService.speakerTurnOn(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/speaker-turn-off")
    public ResponseEntity<?> speakerTurnOff(@RequestBody ScenarioSpeakerCommandRequest request) {
        try {
            Map<String, Object> result = scenarioService.speakerTurnOff(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

