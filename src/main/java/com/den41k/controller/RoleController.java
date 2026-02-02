package com.den41k.controller;

import com.den41k.model.Role;
import com.den41k.model.RolePermission;
import com.den41k.service.RoleService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
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
            List<Role> roles = roleService.getAllRoles();

            List<Map<String, Object>> rolesWithUsage = new ArrayList<>();
            for (Role role : roles) {
                Map<String, Object> roleData = new HashMap<>();
                roleData.put("role", role);
                roleData.put("inUse", roleService.isRoleInUse(role.getId()));
                roleData.put("userCount", roleService.countUsersWithRole(role.getId()));
                rolesWithUsage.add(roleData);
            }

            model.put("email", email);
            model.put("rolesWithUsage", rolesWithUsage);
            model.put("permissions", RolePermission.values());
        }
        return model;
    }

    @Post(consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public HttpResponse<?> saveRole(@Body Map<String, String> formData) {
        try {
            Role role = new Role();
            role.setName(formData.get("name"));

            role.setProjectPermission(parsePermission(formData.get("projectPermission")));
            role.setTaskPermission(parsePermission(formData.get("taskPermission")));
            role.setUserPermission(parsePermission(formData.get("userPermission")));
            role.setRolePermission(parsePermission(formData.get("rolePermission")));

            roleService.save(role);
            return HttpResponse.redirect(URI.create("/admin/roles"));
        } catch (Exception e) {
            e.printStackTrace();
            return HttpResponse.badRequest().body("Ошибка при создании роли: " + e.getMessage());
        }
    }

    @Post(value = "/update", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
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
            e.printStackTrace();
            return HttpResponse.badRequest().body("Ошибка при обновлении роли: " + e.getMessage());
        }
    }

    @Post(value = "/delete/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public HttpResponse<?> deleteRole(Long id) {
        try {
            if (roleService.isRoleInUse(id)) {
                long userCount = roleService.countUsersWithRole(id);
                return HttpResponse.badRequest()
                        .body("Невозможно удалить роль: она используется " + userCount +
                                " пользовател" + getPlural(userCount) + ". Сначала измените роль у этих пользователей.");
            }

            roleService.deleteById(id);
            return HttpResponse.redirect(URI.create("/admin/roles"));

        } catch (Exception e) {
            e.printStackTrace();
            return HttpResponse.badRequest().body("Ошибка при удалении роли: " + e.getMessage());
        }
    }

    @Get("/edit/{id}")
    @View("admin/roleForm")
    public Map<String, Object> editRole(Long id, Session session) {
        String email = session.get("email", String.class).orElse(null);
        if (email == null) {
            return Map.of("error", "Доступ запрещён");
        }

        Role role = roleService.findById(id)
                .orElseThrow(() -> new RuntimeException("Роль не найдена"));

        Map<String, Object> model = new HashMap<>();
        model.put("email", email);
        model.put("role", role);
        model.put("permissions", RolePermission.values());
        model.put("editing", true);
        return model;
    }

    private String getPlural(long count) {
        if (count % 10 == 1 && count % 100 != 11) {
            return "ем";
        } else if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) {
            return "ями";
        } else {
            return "ями";
        }
    }

    private RolePermission parsePermission(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Право не может быть пустым");
        }
        return RolePermission.valueOf(value);
    }
}