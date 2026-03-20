package com.example.smarthome.security;

import com.example.smarthome.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(String username, String rawPassword) {
        registerWithRole(username, rawPassword, AppRole.USER);
    }

    @Transactional
    public void registerWithRole(String username, String rawPassword, AppRole role) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username is required");
        PasswordValidator.validate(rawPassword);
        if (appUserRepository.findByUsername(username).isPresent()) throw new IllegalArgumentException("username already used");

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        appUserRepository.save(user);
    }
}

