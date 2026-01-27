package com.den41k.controller;

import com.den41k.model.*;
import com.den41k.service.ProjectService;
import com.den41k.service.TaskService;
import com.den41k.service.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.session.Session;
import io.micronaut.views.View;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller("/projects/{projectId}/task")
public class TaskController {

    private final UserService userService;
    private final ProjectService projectService;
    private final TaskService taskService;

    public TaskController(UserService userService, ProjectService projectService, TaskService taskService) {
        this.userService = userService;
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @Get("/create")
    @View("taskCreate")
    public Map<String, Object> showCreateForm(Long projectId, Session session) {
        String email = session.get("email", String.class).orElse(null);
        if (email == null) {
            return Map.of("error", "Требуется авторизация");
        }

        Optional<Project> projectOpt = projectService.findById(projectId);
        if (projectOpt.isEmpty()) {
            return Map.of("error", "Проект не найден");
        }

        Project project = projectOpt.get();
        List<Task> tasks = taskService.findByProjectId(projectId);
        List<User> allUsers = userService.getAllUsers();

        Map<String, Object> model = new HashMap<>();
        model.put("email", email);
        model.put("project", project);
        model.put("tasks", tasks);
        model.put("allUsers", allUsers);
        return model;
    }

    @Post(value = "/create", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> createTask(
            Long projectId,
            @Body Map<String, String> formData,
            Session session) {

        String email = session.get("email", String.class).orElse(null);
        if (email == null) {
            return HttpResponse.redirect(URI.create("../auth"));
        }

        Optional<User> currentUser = userService.findByEmail(email);
        if (currentUser.isEmpty()) {
            return HttpResponse.redirect(URI.create("../auth"));
        }

        Optional<Project> projectOpt = projectService.findById(projectId);
        if (projectOpt.isEmpty()) {
            return HttpResponse.redirect(URI.create("../projects"));
        }
        Project project = projectOpt.get();
        String title = formData.get("title");
        String description = formData.get("description");
        String taskStatusStr = formData.get("taskStatus");
        String priorityStr = formData.get("priority");
        String deadLineStr = formData.get("deadLine");
        String executorEmail = formData.get("executorEmail");
        if (title == null || title.trim().isEmpty() ||
                taskStatusStr == null || priorityStr == null) {
            return HttpResponse.redirect(URI.create("/projects/" + projectId + "/task/create"));
        }
        TaskStatus status = TaskStatus.valueOf(taskStatusStr);
        Priority priority = Priority.valueOf(priorityStr);
        User executor = null;
        String executorIdStr = formData.get("executorId");
        if (executorIdStr != null && !executorIdStr.isEmpty()) {
            try {
                Long executorId = Long.parseLong(executorIdStr);
                executor = userService.findById(executorId).orElse(null);
            } catch (NumberFormatException e) {
            }
        }
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setTaskStatus(status);
        task.setPriority(priority);
        task.setProject(project);
        task.setTaskCreator(currentUser.get());
        task.setTaskExecutor(executor);
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        if (deadLineStr != null && !deadLineStr.isEmpty()) {
            task.setDeadLine(LocalDate.parse(deadLineStr));
        }
        taskService.saveTask(task);
        return HttpResponse.redirect(URI.create("/projects/" + projectId));
    }
}