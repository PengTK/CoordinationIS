package com.den41k.controller;

import com.den41k.model.Role;
import com.den41k.model.User;
import com.den41k.repository.ChatParticipantRepository;
import com.den41k.repository.ChatRepository;
import com.den41k.repository.MessageRepository;
import com.den41k.service.*;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.session.Session;
import io.micronaut.transaction.annotation.Transactional;
import io.micronaut.views.View;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@Controller("/admin/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final CommentService commentService;

    @Inject
    ChatParticipantRepository chatParticipantRepository;

    @Inject
    ChatRepository chatRepository;

    @Inject
    MessageRepository messageRepository;

    public UserController(UserService userService,
                          RoleService roleService,
                          ProjectService projectService,
                          TaskService taskService,
                          CommentService commentService) {  // ← добавили в конструктор
        this.userService = userService;
        this.roleService = roleService;
        this.projectService = projectService;
        this.taskService = taskService;
        this.commentService = commentService;
    }


    @Get
    @View("admin/users")
    public Map<String, Object> listUsers(
            @Nullable String query,
            @Nullable Long roleId,
            @Nullable String sort,
            Session session) {

        String email = session.get("email", String.class).orElse(null);
        if (email == null) {
            return Map.of("error", "Доступ запрещён");
        }

        String effectiveSort = (sort != null) ? sort : "oldest";

        List<User> users = userService.searchUsers(query, roleId, effectiveSort);
        List<Role> allRoles = roleService.getAllRoles();

        Map<String, Object> model = new HashMap<>();
        model.put("email", email);
        model.put("users", users);
        model.put("allRoles", allRoles);
        model.put("query", query);
        model.put("roleId", roleId);
        model.put("sort", effectiveSort);
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

    @Post(consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> saveUser(@Body Map<String, String> formData) {
        User user = new User();
        user.setName(formData.get("name"));
        user.setSureName(formData.get("sureName"));
        user.setPatronymic(formData.get("patronymic"));
        user.setEmail(formData.get("email"));
        user.setPassword(formData.get("password"));
        user.setCreatedAt(LocalDateTime.now());
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12)));

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

    @Post(value = "/update", consumes = MediaType.APPLICATION_FORM_URLENCODED)
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
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12)));
        }

        userService.save(user);
        return HttpResponse.redirect(URI.create("/admin/users"));
    }

    @Post(value = "/delete/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public HttpResponse<?> deleteUser(Long id) {
        chatParticipantRepository.deleteByUserId(id);
        chatRepository.deleteByUserId(id);
        messageRepository.deleteByUserId(id);

        commentService.clearCommentAuthor(id);
        projectService.clearProjectCreator(id);
        taskService.clearTaskCreator(id);
        taskService.clearTaskExecutor(id);
        taskService.clearApprover(id);

        userService.deleteById(id);

        return HttpResponse.redirect(URI.create("/admin/users"));
    }

}