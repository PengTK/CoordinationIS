package com.den41k.service;

import com.den41k.model.Role;
import com.den41k.model.User;
import com.den41k.repository.RoleRepository;
import com.den41k.repository.UserRepository;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

@Singleton
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleService(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Role findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Transactional
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    @Transactional
    public Role save(Role role) {
        return roleRepository.merge(role);
    }

    @Transactional
    public void deleteById(Long id) {
        roleRepository.deleteById(id);
    }

    public boolean isRoleInUse(Long roleId) {
        return userRepository.countByRoleId(roleId) > 0;
    }

    public long countUsersWithRole(Long roleId) {
        return userRepository.countByRoleId(roleId);
    }
}
