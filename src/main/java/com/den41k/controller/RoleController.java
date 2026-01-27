package com.den41k.controller;

import com.den41k.model.Role;
import com.den41k.model.RolePermission;
import com.den41k.service.RoleService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.session.Session;
import io.micronaut.views.View;
import io.micronaut.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;

@Controller("/admin/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Get
    @View("admin/roles")
    public Map<String, Object> listRoles(Session session) {
        Map<String, Object> model = new HashMap<>();
        String email = session.get("email", String.class).orElse(null);
        if (email != null) {
            model.put("email", email);
            model.put("roles", roleService.getAllRoles());
            model.put("permissions", RolePermission.values());
        }
        return model;
    }

    @Post(consumes = "application/x-www-form-urlencoded")
    @Transactional// ← важно для сохранения связей
    public HttpResponse<?> saveRole(@Body Map<String, String> formData) {
        try {
            Role role = new Role();
            role.setName(formData.get("name"));

            // Безопасное преобразование enum
            role.setProjectPermission(parsePermission(formData.get("projectPermission")));
            role.setTaskPermission(parsePermission(formData.get("taskPermission")));
            role.setUserPermission(parsePermission(formData.get("userPermission")));
            role.setRolePermission(parsePermission(formData.get("rolePermission")));

            roleService.save(role);
            return HttpResponse.redirect(URI.create("/admin/roles"));
        } catch (Exception e) {
            // В реальности — логировать ошибку
            return HttpResponse.badRequest().body("Ошибка при создании роли");
        }
    }

    @Post(value = "/update", consumes = "application/x-www-form-urlencoded")
    @Transactional // ← важно при обновлении
    public HttpResponse<?> updateRole(@Body Map<String, String> formData) {
        try {
            Long id = Long.valueOf(formData.get("id"));
            Role role = roleService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Роль не найдена"));

            role.setName(formData.get("name"));
            role.setProjectPermission(parsePermission(formData.get("projectPermission")));
            role.setTaskPermission(parsePermission(formData.get("taskPermission")));
            role.setUserPermission(parsePermission(formData.get("userPermission")));
            role.setRolePermission(parsePermission(formData.get("rolePermission")));

            roleService.save(role);
            return HttpResponse.redirect(URI.create("/admin/roles"));
        } catch (Exception e) {
            return HttpResponse.badRequest().body("Ошибка при обновлении роли");
        }
    }

    @Post(value = "/delete/{id}", consumes = "application/x-www-form-urlencoded")
    @Transactional
    public HttpResponse<?> deleteRole(Long id) {
        try {
            roleService.deleteById(id);
        } catch (Exception e) {
            // Например, если роль используется пользователями
        }
        return HttpResponse.redirect(URI.create("/admin/roles"));
    }

    // Вспомогательный метод: безопасное преобразование строки в RolePermission
    private RolePermission parsePermission(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Право не может быть пустым");
        }
        return RolePermission.valueOf(value);
    }
}