package com.example.smarthome.controller;

import com.example.smarthome.service.AdminResetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminResetService adminResetService;

    public AdminController(AdminResetService adminResetService) {
        this.adminResetService = adminResetService;
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestParam(name = "confirm", defaultValue = "false") boolean confirm) {
        if (!confirm) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("confirm=true required");
        }
        Map<String, Long> deleted = adminResetService.resetAll();
        return ResponseEntity.ok(deleted);
    }
}

