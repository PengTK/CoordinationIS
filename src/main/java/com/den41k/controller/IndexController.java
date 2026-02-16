package com.den41k.controller;

import com.den41k.model.Role;
import com.den41k.model.RolePermission;
import com.den41k.model.User;
import com.den41k.service.ChatService;
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
    private final ChatService chatService;

    public IndexController(UserService userService, ChatService chatService) {
        this.userService = userService;
        this.chatService = chatService;
    }

    @Get
    @View("home")
    public Map<String, Object> showHome(Session session) {
        Map<String, Object> model = new HashMap<>();
        String email = session.get("email", String.class).orElse(null);
        if (email != null) {
            Optional<User> currentUser = userService.findByEmail(email);
            if (currentUser.isPresent()) {
                User user = currentUser.get();
                long totalUnread = chatService.getTotalUnreadMessages(user.getId());

                model.put("email", email);
                model.put("firstName", user.getName());
                model.put("role", user.getRole());
                model.put("totalUnread", totalUnread);
            }
        }
        return model;
    }
}
