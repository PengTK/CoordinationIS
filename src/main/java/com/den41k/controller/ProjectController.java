package com.den41k.controller;

import com.den41k.model.Project;
import com.den41k.model.Role;
import com.den41k.model.Task;
import com.den41k.model.User;
import com.den41k.repository.ProjectRepository;
import com.den41k.service.ProjectService;
import com.den41k.service.TaskService;
import com.den41k.service.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.session.Session;
import io.micronaut.views.View;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller("/projects")
public class ProjectController {

    private final UserService userService;
    private final ProjectService projectService;
    private final TaskService taskService;

    public ProjectController(UserService userService, ProjectService projectService, TaskService taskService) {
        this.userService = userService;
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @Get
    @View("projects")
    public Map<String, Object> getAllProjects(Session session) {
        Map<String, Object> currentUserModel = new HashMap<>();
        String email = session.get("email", String.class).orElse(null);
        if (email != null) {
            Optional<User> currentUser = userService.findByEmail(email);
            String firstName = currentUser.get().getName();
            Role role = currentUser.get().getRole();
            currentUserModel.put("email", email);
            currentUserModel.put("firstName", firstName);
            currentUserModel.put("role", role.getName());
            currentUserModel.put("projects", projectService.getAllProjects());
        }

        return currentUserModel;
    }

    @Get("/create")
    @View("projectsCreate")
    public Map<String, Object> showProjectsCreate(Session session) {
        Map<String, Object> currentUserModel = new HashMap<>();
        String email = session.get("email", String.class).orElse(null);
        if (email != null) {
            Optional<User> currentUser = userService.findByEmail(email);
            String firstName = currentUser.get().getName();
            Role role = currentUser.get().getRole();
            currentUserModel.put("email", email);
            currentUserModel.put("firstName", firstName);
            currentUserModel.put("role", role.getName());
        }
        return currentUserModel;
    }

    @Post(value = "/create", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> register(@Body Map<String, String> formData, Session session) {
        String email = session.get("email", String.class).orElse(null);
        if (email != null) {
            Optional<User> currentUser = userService.findByEmail(email);
            Project project = new Project(formData.get("title"), formData.get("description"), LocalDate.parse(formData.get("deadLine")), currentUser.get());
            projectService.saveProject(project);
            return HttpResponse.redirect(URI.create("../projects"));
        }
        return HttpResponse.redirect(URI.create("../auth"));
    }

    @Get("/{id}")
    @View("projectDetails")
    public Map<String, Object> getProjectDetails(Long id, Session session) {
        Map<String, Object> model = new HashMap<>();

        String email = session.get("email", String.class).orElse(null);
        if (email == null) {
            return Map.of("error", "Доступ запрещён");
        }

        Optional<Project> projectOpt = projectService.findById(id);
        if (projectOpt.isEmpty()) {
            model.put("error", "Проект не найден");
            return model;
        }

        Project project = projectOpt.get();
        List<Task> tasks = taskService.findByProjectId(id);

        model.put("email", email);
        model.put("project", project);
        model.put("tasks", tasks);
        return model;
    }
}
