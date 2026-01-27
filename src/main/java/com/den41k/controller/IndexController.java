package com.den41k.controller;

import com.den41k.model.Role;
import com.den41k.model.RolePermission;
import com.den41k.model.User;
import com.den41k.service.UserService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.session.Session;
import io.micronaut.views.ModelAndView;
import io.micronaut.views.View;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class IndexController {

    private final UserService userService;

    public IndexController(UserService userService) {
        this.userService = userService;
    }


    @Get
    @View("home")
    public Map<String, Object> index(Session session) {
        Map<String, Object> currentUserModel = new HashMap<>();
        String email = session.get("email", String.class).orElse(null);
        if (email != null) {
            Optional<User> currentUser = userService.findByEmail(email);
            String firstName = currentUser.get().getName();
            Role role;
            if (currentUser.get().getRole() != null) {
                role = currentUser.get().getRole();
            } else {
                role = new Role("Гость", RolePermission.NO_PERMS, RolePermission.NO_PERMS, RolePermission.NO_PERMS, RolePermission.NO_PERMS);
            }
            currentUserModel.put("email", email);
            currentUserModel.put("firstName", firstName);
            currentUserModel.put("role", role);
        }

        return currentUserModel;
    }
}
