package com.example.smarthome.controller;

import com.example.smarthome.security.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.registerUser(request.username, request.password);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", "registered", "role", "USER"));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest request) {
        authService.registerWithRole(request.username, request.password, AppRole.ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", "registered"));
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("csrfToken", token.getToken());
    }

    public static class RegisterRequest {
        @NotBlank
        public String username;

        @NotBlank
        public String password;
    }
}

