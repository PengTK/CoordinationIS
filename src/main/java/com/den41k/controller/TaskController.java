package com.den41k.controller;

import com.den41k.model.*;
import com.den41k.service.CommentService;
import com.den41k.service.ProjectService;
import com.den41k.service.TaskService;
import com.den41k.service.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.session.Session;
import io.micronaut.views.View;
import io.micronaut.transaction.annotation.Transactional;

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
    private final CommentService commentService;

    public TaskController(UserService userService, ProjectService projectService,
                          TaskService taskService, CommentService commentService) {
        this.userService = userService;
        this.projectService = projectService;
        this.taskService = taskService;
        this.commentService = commentService;
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

    @Get("/{taskId}")
    @View("taskDetails")
    public Map<String, Object> getTaskDetails(Long projectId, Long taskId, Session session) {
        Map<String, Object> model = new HashMap<>();

        String email = session.get("email", String.class).orElse(null);
        if (email == null) {
            return Map.of("error", "Доступ запрещён");
        }

        Optional<Project> projectOpt = projectService.findById(projectId);
        if (projectOpt.isEmpty()) {
            model.put("error", "Проект не найден");
            return model;
        }

        Optional<Task> taskOpt = taskService.findById(taskId);
        if (taskOpt.isEmpty()) {
            model.put("error", "Задача не найдена");
            return model;
        }

        Project project = projectOpt.get();
        Task task = taskOpt.get();
        List<Task> tasks = taskService.findByProjectId(projectId);
        List<User> allUsers = userService.getAllUsers();
        List<Comment> comments = commentService.getCommentsByTaskId(taskId);
        long commentCount = commentService.countCommentsByTaskId(taskId);

        Optional<User> currentUser = userService.findByEmail(email);
        boolean canEdit = currentUser.isPresent() &&
                (currentUser.get().getRole().getName().equals("ADMIN") ||
                        currentUser.get().getId().equals(task.getTaskCreator().getId()));

        model.put("email", email);
        model.put("project", project);
        model.put("task", task);
        model.put("tasks", tasks);
        model.put("allUsers", allUsers);
        model.put("canEdit", canEdit);
        model.put("comments", comments);
        model.put("commentCount", commentCount);
        model.put("currentUser", currentUser.get());

        return model;
    }

    @Post(value = "/{taskId}/update", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public HttpResponse<?> updateTask(Long projectId, Long taskId, @Body Map<String, String> formData) {
        try {
            Optional<Task> taskOpt = taskService.findById(taskId);
            if (taskOpt.isEmpty()) {
                return HttpResponse.badRequest().body("Задача не найдена");
            }

            Task task = taskOpt.get();

            task.setTitle(formData.get("title"));
            task.setDescription(formData.get("description"));
            task.setTaskStatus(TaskStatus.valueOf(formData.get("taskStatus")));
            task.setPriority(Priority.valueOf(formData.get("priority")));

            String deadLineStr = formData.get("deadLine");
            if (deadLineStr != null && !deadLineStr.trim().isEmpty()) {
                task.setDeadLine(LocalDate.parse(deadLineStr));
            } else {
                task.setDeadLine(null);
            }

            String executorIdStr = formData.get("executorId");
            if (executorIdStr != null && !executorIdStr.trim().isEmpty()) {
                Long executorId = Long.parseLong(executorIdStr);
                User executor = userService.findById(executorId).orElse(null);
                task.setTaskExecutor(executor);
            } else {
                task.setTaskExecutor(null);
            }

            task.setUpdatedAt(LocalDateTime.now());
            taskService.saveTask(task);

            return HttpResponse.redirect(URI.create("/projects/" + projectId + "/task/" + taskId));

        } catch (Exception e) {
            e.printStackTrace();
            return HttpResponse.badRequest().body("Ошибка при обновлении задачи: " + e.getMessage());
        }
    }

    @Post(value = "/{taskId}/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public HttpResponse<?> deleteTask(Long projectId, Long taskId, Session session) {
        try {
            String email = session.get("email", String.class).orElse(null);
            if (email == null) {
                return HttpResponse.redirect(URI.create("/auth"));
            }

            Optional<Task> taskOpt = taskService.findById(taskId);
            if (taskOpt.isEmpty()) {
                return HttpResponse.badRequest().body("Задача не найдена");
            }

            Task task = taskOpt.get();

            Optional<User> currentUser = userService.findByEmail(email);
            boolean canDelete = currentUser.isPresent() &&
                    (currentUser.get().getRole().getName().equals("ADMIN") ||
                            currentUser.get().getId().equals(task.getTaskCreator().getId()));

            if (!canDelete) {
                return HttpResponse.badRequest().body("У вас нет прав на удаление этой задачи");
            }

            List<Comment> comments = commentService.getCommentsByTaskId(taskId);
            for (Comment comment : comments) {
                commentService.deleteComment(comment.getId());
            }

            taskService.deleteTask(taskId);

            return HttpResponse.redirect(URI.create("/projects/" + projectId));

        } catch (Exception e) {
            e.printStackTrace();
            return HttpResponse.badRequest().body("Ошибка при удалении задачи: " + e.getMessage());
        }
    }

    @Post(value = "/{taskId}/comment", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public HttpResponse<?> addComment(Long projectId, Long taskId, @Body Map<String, String> formData, Session session) {
        try {
            String email = session.get("email", String.class).orElse(null);
            if (email == null) {
                return HttpResponse.redirect(URI.create("/auth"));
            }

            Optional<User> currentUser = userService.findByEmail(email);
            if (currentUser.isEmpty()) {
                return HttpResponse.redirect(URI.create("/auth"));
            }

            Optional<Task> taskOpt = taskService.findById(taskId);
            if (taskOpt.isEmpty()) {
                return HttpResponse.badRequest().body("Задача не найдена");
            }

            Task task = taskOpt.get();
            String content = formData.get("content");

            if (content == null || content.trim().isEmpty()) {
                return HttpResponse.badRequest().body("Комментарий не может быть пустым");
            }

            commentService.addComment(content.trim(), task, currentUser.get());

            return HttpResponse.redirect(URI.create("/projects/" + projectId + "/task/" + taskId));

        } catch (Exception e) {
            e.printStackTrace();
            return HttpResponse.badRequest().body("Ошибка при добавлении комментария: " + e.getMessage());
        }
    }

    @Post(value = "/{taskId}/comment/{commentId}/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public HttpResponse<?> deleteComment(Long projectId, Long taskId, Long commentId, Session session) {
        try {
            String email = session.get("email", String.class).orElse(null);
            if (email == null) {
                return HttpResponse.redirect(URI.create("/auth"));
            }

            Optional<User> currentUser = userService.findByEmail(email);
            if (currentUser.isEmpty()) {
                return HttpResponse.redirect(URI.create("/auth"));
            }

            Optional<Comment> commentOpt = commentService.getCommentsByTaskId(taskId).stream()
                    .filter(c -> c.getId().equals(commentId))
                    .findFirst();

            if (commentOpt.isEmpty()) {
                return HttpResponse.badRequest().body("Комментарий не найден");
            }

            Comment comment = commentOpt.get();
            boolean canDelete = currentUser.get().getRole().getName().equals("ADMIN") ||
                    currentUser.get().getId().equals(comment.getAuthor().getId());

            if (!canDelete) {
                return HttpResponse.badRequest().body("У вас нет прав на удаление этого комментария");
            }

            commentService.deleteComment(commentId);

            return HttpResponse.redirect(URI.create("/projects/" + projectId + "/task/" + taskId));

        } catch (Exception e) {
            e.printStackTrace();
            return HttpResponse.badRequest().body("Ошибка при удалении комментария: " + e.getMessage());
        }
    }
}