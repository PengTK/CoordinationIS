package com.den41k.service;

import com.den41k.model.Role;
import com.den41k.model.Task;
import com.den41k.model.User;
import com.den41k.repository.*;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.*;

@Singleton
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> searchUsers(String query, Long roleId, String sort) {
        String queryWithWildcards = null;
        if (query != null && !query.trim().isEmpty()) {
            queryWithWildcards = "%" + query.trim().toLowerCase() + "%";
        }

        if ("oldest".equals(sort)) {
            return userRepository.searchOldestFirst(queryWithWildcards, roleId);
        } else {
            return userRepository.search(queryWithWildcards, roleId);
        }
    }

    @Transactional
    public User registerUser(User user) {
        Optional<User> existing = userRepository.findByEmail(user.getEmail());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        return userRepository.save(user);
    }

    @Transactional
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public Optional<User> findById(Long id) { return userRepository.findById(id); }


    @Transactional
    public User save(User user) {
        return userRepository.merge(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public List<User> findAll() {
        return userRepository.findAll();
    }

}
