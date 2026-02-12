package com.den41k.controller;

import com.den41k.dto.MessageDto;
import com.den41k.model.*;
import com.den41k.repository.ChatRepository;
import com.den41k.service.ChatService;
import com.den41k.service.ProjectService;
import com.den41k.service.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.session.Session;
import io.micronaut.views.View;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller("/chats")
public class ChatController {

    ChatService chatService;
    UserService userService;
    ProjectService projectService;

    public ChatController(ChatService chatService, UserService userService, ProjectService projectService) {
        this.chatService = chatService;
        this.userService = userService;
        this.projectService = projectService;
    }

    // Список всех чатов пользователя
    @Get("/")
    @View("chats")
    public Map<String, Object> listChats(Session session) {
        Map<String, Object> model = new HashMap<>();
        
        String email = session.get("email", String.class).orElse(null);
        if (email == null) {
            model.put("error", "Доступ запрещён");
            return model;
        }
        
        Optional<User> currentUser = userService.findByEmail(email);
        if (currentUser.isEmpty()) {
            model.put("error", "Пользователь не найден");
            return model;
        }
        
        User user = currentUser.get();
        
        // Получаем все чаты пользователя
        List<Chat> chats = chatService.getUserChats(user.getId());
        
        // Получаем количество непрочитанных сообщений для каждого чата
        Map<Long, Long> unreadCounts = new HashMap<>();
        for (Chat chat : chats) {
            unreadCounts.put(chat.getId(), chatService.getUnreadMessagesCount(chat.getId(), user.getId()));
        }
        
        // Получаем всех пользователей для создания чатов
        List<User> allUsers = userService.findAll();
        
        model.put("email", email);
        model.put("currentUser", user);
        model.put("chats", chats);
        model.put("unreadCounts", unreadCounts);
        model.put("allUsers", allUsers);
        model.put("totalUnread", chatService.getTotalUnreadMessages(user.getId()));
        
        return model;
    }
    
    // Создать личный чат
    @Post(value = "/private", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> createPrivateChat(@Body Map<String, String> body, Session session) {
        String email = session.get("email", String.class).orElse(null);
        if (email == null) {
            return HttpResponse.unauthorized();
        }
        
        Optional<User> currentUser = userService.findByEmail(email);
        if (currentUser.isEmpty()) {
            return HttpResponse.badRequest();
        }
        
        Long userId1 = currentUser.get().getId();
        Long userId2;
        
        try {
            userId2 = Long.parseLong(body.get("userId"));
        } catch (NumberFormatException e) {
            return HttpResponse.badRequest();
        }
        
        // Проверяем, что пользователь не создаёт чат с самим собой
        if (userId1.equals(userId2)) {
            return HttpResponse.badRequest();
        }
        
        Chat chat = chatService.createPrivateChat(userId1, userId2);
        
        return HttpResponse.redirect(URI.create("/chats/" + chat.getId()));
    }
    
    // Создать групповой чат
    @Post(value = "/group", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> createGroupChat(@Body Map<String, Object> body, Session session) {
        String email = session.get("email", String.class).orElse(null);
        if (email == null) {
            return HttpResponse.unauthorized();
        }
        
        Optional<User> currentUser = userService.findByEmail(email);
        if (currentUser.isEmpty()) {
            return HttpResponse.badRequest();
        }
        
        String name = (String) body.get("name");
        if (name == null || name.trim().isEmpty()) {
            return HttpResponse.badRequest();
        }
        
        @SuppressWarnings("unchecked")
        List<String> participantIdsStr = (List<String>) body.get("participants");
        List<Long> participantIds = participantIdsStr.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
        
        // Добавляем текущего пользователя
        participantIds.add(currentUser.get().getId());
        
        Chat chat = chatService.createGroupChat(name, currentUser.get().getId(), participantIds);
        
        return HttpResponse.redirect(URI.create("/chats/" + chat.getId()));
    }
    
    // Создать чат проекта
    @Post(value = "/project/{projectId}", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> createProjectChat(Long projectId, Session session) {
        String email = session.get("email", String.class).orElse(null);
        if (email == null) {
            return HttpResponse.unauthorized();
        }
        
        Optional<Project> projectOpt = projectService.findById(projectId);
        if (projectOpt.isEmpty()) {
            return HttpResponse.badRequest();
        }
        
        Project project = projectOpt.get();
        
        // Создаём чат проекта
        Chat chat = chatService.createProjectChat(project);
        
        return HttpResponse.redirect(URI.create("/chats/" + chat.getId()));
    }

    @Get("/{chatId}")
    @View("chat")
    public Map<String, Object> viewChat(Long chatId, Session session) {
        Map<String, Object> model = new HashMap<>();

        String email = session.get("email", String.class).orElse(null);
        if (email == null) {
            model.put("error", "Доступ запрещён");
            return model;
        }

        Optional<User> currentUser = userService.findByEmail(email);
        if (currentUser.isEmpty()) {
            model.put("error", "Пользователь не найден");
            return model;
        }

        User user = currentUser.get();

        // Проверяем, что чат существует (используем оригинальную сущность Chat)
        Optional<Chat> chatOpt = chatService.findById(chatId);
        if (chatOpt.isEmpty()) {
            model.put("error", "Чат не найден");
            return model;
        }

        Chat chat = chatOpt.get();

        // Проверяем, что пользователь является участником чата
        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(user.getId()));

        if (!isParticipant) {
            model.put("error", "Вы не являетесь участником этого чата");
            return model;
        }

        // Получаем последние 50 сообщений (используем DTO)
        List<MessageDto> messages = chatService.getChatMessages(chatId, 50);
        Collections.reverse(messages); // Старые сообщения сверху

        // Получаем участников чата
        List<ChatParticipant> participants = chatService.getChatParticipants(chatId);

        // Помечаем сообщения как прочитанные
        chatService.markMessagesAsRead(chatId, user.getId());

        model.put("email", email);
        model.put("currentUser", user);
        model.put("chat", chat);          // Оригинальный объект Chat
        model.put("messages", messages);  // Список MessageDto
        model.put("participants", participants);

        return model;
    }

    @Get("/{chatId}/messages/since")
    public List<MessageDto> getNewMessages(@PathVariable Long chatId, @QueryValue("since") String sinceStr, Session session) {
        // Проверка сессии
        String email = session.get("email", String.class).orElse(null);
        if (email == null) {
            return Collections.emptyList();
        }

        Optional<User> currentUser = userService.findByEmail(email);
        if (currentUser.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime since = LocalDateTime.parse(sinceStr, formatter);

            return chatService.getNewMessagesSince(chatId, since);
        } catch (Exception e) {
            System.err.println("Ошибка в getNewMessages: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Post(value = "/{chatId}/message", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> sendMessage(Long chatId, @QueryValue("content") String content, Session session) {
        String email = session.get("email", String.class).orElse(null);
        if (email == null) {
            return HttpResponse.unauthorized();
        }

        Optional<User> currentUser = userService.findByEmail(email);
        if (currentUser.isEmpty()) {
            return HttpResponse.badRequest("Пользователь не найден");
        }

        if (content == null || content.trim().isEmpty()) {
            return HttpResponse.badRequest("Сообщение не может быть пустым");
        }

        try {
            chatService.sendMessage(chatId, currentUser.get().getId(), content.trim());
            return HttpResponse.ok();
        } catch (Exception e) {
            return HttpResponse.badRequest("Ошибка: " + e.getMessage());
        }
    }
}