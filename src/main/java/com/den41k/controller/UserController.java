package com.den41k.controller;

import com.den41k.model.Role;
import com.den41k.model.User;
import com.den41k.service.RoleService;
import com.den41k.service.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.session.Session;
import io.micronaut.views.View;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@Controller("/admin/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Get
    @View("admin/users")
    public Map<String, Object> listUsers(Session session) {
        Map<String, Object> model = new HashMap<>();
        String email = session.get("email", String.class).orElse(null);
        if (email != null) {
            model.put("email", email);
            model.put("users", userService.getAllUsers());
            model.put("role", userService.findByEmail(email).get().getRole());
        }
        return model;
    }

    @Get("/create")
    @View("admin/userForm")
    public Map<String, Object> showCreateForm(Session session) {
        Map<String, Object> model = new HashMap<>();
        String email = session.get("email", String.class).orElse(null);
        if (email != null) {
            model.put("email", email);
            model.put("roles", roleService.getAllRoles());
            model.put("user", new User());
            model.put("editing", false);
        }
        return model;
    }

    @Post(consumes = "application/x-www-form-urlencoded")
    public HttpResponse<?> saveUser(@Body Map<String, String> formData) {
        User user = new User();
        user.setName(formData.get("name"));
        user.setSureName(formData.get("sureName"));
        user.setPatronymic(formData.get("patronymic"));
        user.setEmail(formData.get("email"));
        user.setPassword(formData.get("password"));
        user.setCreatedAt(LocalDateTime.now());
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(15)));

        Role roleRef = new Role();
        roleRef.setId(Long.parseLong(formData.get("roleId")));
        user.setRole(roleRef);

        userService.save(user);
        return HttpResponse.redirect(URI.create("/admin/users"));
    }

    @Get("/edit/{id}")
    @View("admin/userForm")
    public Map<String, Object> editUser(Long id, Session session) {
        Map<String, Object> model = new HashMap<>();
        String email = session.get("email", String.class).orElse(null);
        if (email != null) {
            User user = userService.findById(id).orElseThrow();
            model.put("email", email);
            model.put("user", user);
            model.put("roles", roleService.getAllRoles());
            model.put("editing", true);
        }
        return model;
    }

    @Post(value = "/update", consumes = "application/x-www-form-urlencoded")
    public HttpResponse<?> updateUser(@Body Map<String, String> formData) {
        Long id = Long.parseLong(formData.get("id"));
        User user = userService.findById(id).orElseThrow();

        user.setName(formData.get("name"));
        user.setSureName(formData.get("sureName"));
        user.setPatronymic(formData.get("patronymic"));
        user.setEmail(formData.get("email"));

        Long roleId = Long.parseLong(formData.get("roleId"));
        Role role = roleService.findById(roleId).orElseThrow();
        user.setRole(role);

        if (formData.get("password") != null && !formData.get("password").isEmpty()) {
            user.setPassword(formData.get("password"));
        }

        userService.save(user);
        return HttpResponse.redirect(URI.create("/admin/users"));
    }

    @Post(value = "/delete/{id}", consumes = "application/x-www-form-urlencoded")
    public HttpResponse<?> deleteUser(Long id) {
        userService.deleteById(id);
        return HttpResponse.redirect(URI.create("/admin/users"));
    }
}