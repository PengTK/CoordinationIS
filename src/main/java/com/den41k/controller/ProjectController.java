package com.den41k.controller;

import com.den41k.model.*;
import com.den41k.service.*;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.session.Session;
import io.micronaut.views.View;
import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Controller("/projects")
public class ProjectController {

    private final UserService userService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final CommentService commentService;
    private final CalendarService calendarService;

    public ProjectController(UserService userService, ProjectService projectService, TaskService taskService, CommentService commentService, CalendarService calendarService) {
        this.userService = userService;
        this.projectService = projectService;
        this.taskService = taskService;
        this.commentService = commentService;
        this.calendarService = calendarService;
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
    public HttpResponse<?> createProject(@Body Map<String, String> formData, Session session) {
        String email = session.get("email", String.class).orElse(null);
        if (email != null) {
            Optional<User> currentUser = userService.findByEmail(email);
            Project project = new Project(
                    formData.get("title"),
                    formData.get("description"),
                    LocalDate.parse(formData.get("deadLine")),
                    currentUser.get()
            );
            projectService.saveProject(project);
            return HttpResponse.redirect(URI.create("/projects"));
        }
        return HttpResponse.redirect(URI.create("/auth"));
    }
    @Get("/{id}")
    @View("projectDetails")
    public Map<String, Object> getProjectDetails(
            Long id,
            Session session,
            HttpRequest<?> request,
            @QueryValue(value = "search", defaultValue = "") String search,
            @QueryValue(value = "status", defaultValue = "") String status,
            @QueryValue(value = "priority", defaultValue = "") String priority,
            @QueryValue(value = "executor", defaultValue = "") String executor) {

        Map<String, Object> model = new HashMap<>();

        String email = session.get("email", String.class).orElse(null);
        if (email == null) {
            model.put("error", "Доступ запрещён");
            return model;
        }

        Optional<Project> projectOpt = projectService.findById(id);
        if (projectOpt.isEmpty()) {
            model.put("error", "Проект не найден");
            return model;
        }

        Project project = projectOpt.get();

        List<Task> tasks = taskService.findByProjectIdWithFilters(id, search, status, priority, executor);

        long totalTasksCount = taskService.countByProjectId(id);

        Map<Long, Long> commentCounts = new HashMap<>();
        for (Task task : tasks) {
            commentCounts.put(task.getId(), commentService.countCommentsByTaskId(task.getId()));
        }

        Optional<User> currentUser = userService.findByEmail(email);
        boolean canEdit = currentUser.isPresent() &&
                (currentUser.get().getRole().getName().equals("ADMIN") ||
                        currentUser.get().getId().equals(project.getProjectCreator().getId()));

        List<User> allUsers = userService.findAll();

        String monthParam = request.getParameters().get("month", String.class).orElse(null);
        YearMonth currentMonth;

        if (monthParam != null) {
            try {
                currentMonth = YearMonth.parse(monthParam);
            } catch (Exception e) {
                currentMonth = YearMonth.now();
            }
        } else {
            currentMonth = project.getDeadLine() != null
                    ? YearMonth.from(project.getDeadLine())
                    : YearMonth.now();
        }

        Map<String, Object> calendarData = calendarService.generateCalendarData(project, tasks, currentMonth);
        model.put("calendar", calendarData);

        model.put("search", search);
        model.put("status", status);
        model.put("priority", priority);
        model.put("executor", executor);

        model.put("email", email);
        model.put("project", project);
        model.put("tasks", tasks);
        model.put("totalTasksCount", totalTasksCount);
        model.put("commentCounts", commentCounts);
        model.put("canEdit", canEdit);
        model.put("allUsers", allUsers);

        return model;
    }

    @Get("/{id}/edit")
    @View("projectEdit")
    public Map<String, Object> showEditForm(Long id, Session session) {
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

        Optional<User> currentUser = userService.findByEmail(email);
        if (currentUser.isEmpty() ||
                (!currentUser.get().getRole().getName().equals("ADMIN") &&
                        !currentUser.get().getId().equals(project.getProjectCreator().getId()))) {
            model.put("error", "У вас нет прав на редактирование этого проекта");
            return model;
        }

        model.put("email", email);
        model.put("project", project);
        model.put("firstName", currentUser.get().getName());
        model.put("role", currentUser.get().getRole().getName());

        return model;
    }

    @Post(value = "/{id}/edit", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> updateProject(Long id, @Body Map<String, String> formData, Session session) {
        try {
            String email = session.get("email", String.class).orElse(null);
            if (email == null) {
                return HttpResponse.redirect(URI.create("/auth"));
            }

            Optional<Project> projectOpt = projectService.findById(id);
            if (projectOpt.isEmpty()) {
                return HttpResponse.badRequest().body("Проект не найден");
            }

            Project project = projectOpt.get();

            Optional<User> currentUser = userService.findByEmail(email);
            if (currentUser.isEmpty() ||
                    (!currentUser.get().getRole().getName().equals("ADMIN") &&
                            !currentUser.get().getId().equals(project.getProjectCreator().getId()))) {
                return HttpResponse.badRequest().body("У вас нет прав на редактирование этого проекта");
            }

            String title = formData.get("title");
            String description = formData.get("description");
            String deadLineStr = formData.get("deadLine");

            if (title == null || title.trim().isEmpty()) {
                return HttpResponse.badRequest().body("Название проекта не может быть пустым");
            }

            project.setTitle(title);
            project.setDescription(description);

            if (deadLineStr != null && !deadLineStr.trim().isEmpty()) {
                project.setDeadLine(LocalDate.parse(deadLineStr));
            }

            projectService.saveProject(project);

            return HttpResponse.redirect(URI.create("/projects/" + id));

        } catch (Exception e) {
            System.err.println("Ошибка при обновлении проекта: " + e.getMessage());
            e.printStackTrace();
            return HttpResponse.badRequest().body("Ошибка при обновлении проекта: " + e.getMessage());
        }
    }

    @Post(value = "/{id}/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> deleteProject(Long id, Session session) {
        try {
            String email = session.get("email", String.class).orElse(null);
            if (email == null) {
                return HttpResponse.redirect(URI.create("/auth"));
            }

            Optional<Project> projectOpt = projectService.findById(id);
            if (projectOpt.isEmpty()) {
                return HttpResponse.badRequest().body("Проект не найден");
            }

            Project project = projectOpt.get();

            Optional<User> currentUser = userService.findByEmail(email);
            if (currentUser.isEmpty() ||
                    (!currentUser.get().getRole().getName().equals("ADMIN") &&
                            !currentUser.get().getId().equals(project.getProjectCreator().getId()))) {
                return HttpResponse.badRequest().body("У вас нет прав на удаление этого проекта");
            }

            List<Task> tasks = taskService.findByProjectId(id);

            for (Task task : tasks) {
                List<Comment> comments = commentService.getCommentsByTaskId(task.getId());
                for (Comment comment : comments) {
                    commentService.deleteComment(comment.getId());
                }

                taskService.deleteTask(task.getId());
            }

            projectService.deleteProject(id);

            return HttpResponse.redirect(URI.create("/projects"));

        } catch (Exception e) {
            System.err.println("Ошибка при удалении проекта: " + e.getMessage());
            e.printStackTrace();
            return HttpResponse.badRequest().body("Ошибка при удалении проекта: " + e.getMessage());
        }
    }
}