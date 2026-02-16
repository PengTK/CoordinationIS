package com.den41k.controller;

import com.den41k.model.Task;
import com.den41k.model.User;
import com.den41k.service.TaskService;
import com.den41k.service.UserService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.server.exceptions.HttpServerException;
import io.micronaut.session.Session;
import io.micronaut.views.View;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller("/profile")
public class ProfileController {

    private final UserService userService;
    private final TaskService taskService;

    public ProfileController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    @Get
    @View("profile")
    public Map<String, Object> showProfile(Session session) {
        Optional<Object> userIdOpt = session.get("userId");
        if (userIdOpt.isEmpty()) {
            throw new HttpServerException("Пользователь не авторизован");
        }

        Long userId = (Long) userIdOpt.get();
        Optional<User> userOpt = userService.findById(userId);

        if (userOpt.isEmpty()) {
            throw new HttpServerException("Пользователь не найден");
        }

        User currentUser = userOpt.get();

        List<Task> assignedTasks = taskService.findTasksByExecutorId(currentUser.getId());
        List<Task> reviewingTasks = taskService.findTasksByApproverId(currentUser.getId());

        return Map.of(
                "user", currentUser,
                "assignedTasks", assignedTasks,
                "reviewingTasks", reviewingTasks
        );
    }
}