package com.example.smarthome.controller;

import com.example.smarthome.security.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.username, request.password));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.logout(request.refreshToken));
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf() {
        return Map.of("status", "csrf disabled for stateless jwt");
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities()
        );
    }

    public static class RegisterRequest {
        @NotBlank
        public String username;

        @NotBlank
        public String password;
    }

    public static class LoginRequest {
        @NotBlank
        public String username;

        @NotBlank
        public String password;
    }

    public static class RefreshRequest {
        @NotBlank
        public String refreshToken;
    }
}

