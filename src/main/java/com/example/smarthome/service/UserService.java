package com.example.smarthome.service;

import com.example.smarthome.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final Map<Long, User> storage = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1L);

    public User create(User user) {
        User entity = new User(null, user.getName(), user.getEmail());
        entity.setId(nextId.getAndIncrement());
        storage.put(entity.getId(), entity);
        return entity;
    }

    public Optional<User> getById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<User> getAll() {
        return List.copyOf(storage.values());
    }

    public Optional<User> update(Long id, User user) {
        User existing = storage.get(id);
        if (existing == null) return Optional.empty();
        existing.setName(user.getName());
        existing.setEmail(user.getEmail());
        return Optional.of(existing);
    }

    public boolean delete(Long id) {
        return storage.remove(id) != null;
    }

    public boolean exists(Long id) {
        return storage.containsKey(id);
    }
}
