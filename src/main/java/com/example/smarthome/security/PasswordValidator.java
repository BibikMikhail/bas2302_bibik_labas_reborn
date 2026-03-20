package com.example.smarthome.security;

public final class PasswordValidator {

    private PasswordValidator() {
    }

    public static void validate(String rawPassword) {
        if (rawPassword == null) throw new IllegalArgumentException("Password is required");
        if (rawPassword.length() < 8) throw new IllegalArgumentException("Password must be at least 8 characters");
        if (!rawPassword.matches(".*[^A-Za-z0-9].*")) throw new IllegalArgumentException("Password must contain a special character");
    }
}

