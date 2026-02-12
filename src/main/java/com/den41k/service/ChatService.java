package com.den41k.service;

import com.den41k.dto.MessageDto;
import com.den41k.model.*;
import com.den41k.repository.ChatParticipantRepository;
import com.den41k.repository.ChatRepository;
import com.den41k.repository.MessageRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class ChatService {
    
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;
    
    public ChatService(ChatRepository chatRepository, 
                      ChatParticipantRepository chatParticipantRepository,
                      MessageRepository messageRepository,
                      UserService userService) {
        this.chatRepository = chatRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
    }
    
    // Получить все чаты пользователя
    public List<Chat> getUserChats(Long userId) {
        return chatRepository.findByUserId(userId);
    }
    
    // Создать групповой чат
    @Transactional
    public Chat createGroupChat(String name, Long creatorId, List<Long> participantIds) {
        User creator = userService.findById(creatorId).orElseThrow();
        
        Chat chat = new Chat(ChatType.GROUP, creator);
        chat.setName(name);
        chat = chatRepository.save(chat);
        
        // Добавляем создателя как админа
        ChatParticipant creatorParticipant = new ChatParticipant(chat, creator, true);
        chatParticipantRepository.save(creatorParticipant);
        
        // Добавляем остальных участников
        for (Long participantId : participantIds) {
            if (!participantId.equals(creatorId)) {
                User participant = userService.findById(participantId).orElseThrow();
                ChatParticipant chatParticipant = new ChatParticipant(chat, participant, false);
                chatParticipantRepository.save(chatParticipant);
            }
        }
        
        return chat;
    }
    
    // Создать чат проекта
    @Transactional
    public Chat createProjectChat(Project project) {
        User creator = project.getProjectCreator();
        
        Chat chat = new Chat(ChatType.PROJECT, creator);
        chat.setName("Чат проекта: " + project.getTitle());
        chat.setProject(project);
        chat = chatRepository.save(chat);
        
        // Добавляем создателя проекта как админа
        ChatParticipant creatorParticipant = new ChatParticipant(chat, creator, true);
        chatParticipantRepository.save(creatorParticipant);
        
        return chat;
    }
    
    // Получить участников чата
    public List<ChatParticipant> getChatParticipants(Long chatId) {
        return chatParticipantRepository.findByChatId(chatId);
    }
    
    // Пометить сообщения как прочитанные
    @Transactional
    public void markMessagesAsRead(Long chatId, Long userId) {
        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatIdAndUserId(chatId, userId);
        participant.ifPresent(p -> {
            p.setLastReadAt(LocalDateTime.now());
            chatParticipantRepository.update(p);
        });
    }
    
    // Получить количество непрочитанных сообщений
    public long getUnreadMessagesCount(Long chatId, Long userId) {
        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatIdAndUserId(chatId, userId);
        if (participant.isEmpty()) {
            return 0;
        }
        
        LocalDateTime lastRead = participant.get().getLastReadAt();
        if (lastRead == null) {
            return messageRepository.countByChatId(chatId);
        }
        
        return chatRepository.countUnreadMessages(chatId, lastRead);
    }
    
    // Получить все непрочитанные сообщения пользователя
    public long getTotalUnreadMessages(Long userId) {
        List<Chat> chats = getUserChats(userId);
        return chats.stream()
                .mapToLong(chat -> getUnreadMessagesCount(chat.getId(), userId))
                .sum();
    }

    @Transactional
    public Optional<Chat> findById(Long id){
        return chatRepository.findById(id);
    }

    @Transactional
    public List<MessageDto> getChatMessages(Long chatId, int limit) {
        List<Message> messages = messageRepository.findAllByChatId(chatId);

        // Берём последние N сообщений, но сохраняем хронологический порядок
        int fromIndex = Math.max(0, messages.size() - limit);
        List<Message> lastMessages = messages.subList(fromIndex, messages.size());

        return lastMessages.stream()
                .map(m -> new MessageDto(
                        m.getId(),
                        m.getChat().getId(),
                        m.getAuthor().getId(),
                        m.getAuthor().getName(),
                        m.getContent(),
                        m.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MessageDto> getNewMessagesSince(Long chatId, LocalDateTime since) {
        List<Message> messages = messageRepository.findMessagesSince(chatId, since);

        return messages.stream()
                .map(m -> new MessageDto(
                        m.getId(),
                        m.getChat().getId(),
                        m.getAuthor().getId(),
                        m.getAuthor().getName(),
                        m.getContent(),
                        m.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public boolean canAccessChats(User user) {
        Role role = user.getRole();
        return role != null &&
                (role.getChatPermission() == RolePermission.ALL_PERMS ||
                        role.getChatPermission() == RolePermission.READ_ONLY);
    }

    public boolean canCreateChats(User user) {
        Role role = user.getRole();
        return role != null && role.getChatPermission() == RolePermission.ALL_PERMS;
    }

    public boolean canSendMessage(User user, Chat chat) {
        // Проверяем базовый доступ к чатам
        if (!canAccessChats(user)) {
            return false;
        }

        // Проверяем, что пользователь является участником чата
        return chat.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(user.getId()));
    }

    // Обновленный метод создания личного чата
    @Transactional
    public Chat createPrivateChat(Long userId1, Long userId2) {
        User user1 = userService.findById(userId1).orElseThrow();
        User user2 = userService.findById(userId2).orElseThrow();

        // Проверяем права на создание чатов
        if (!canCreateChats(user1)) {
            throw new RuntimeException("У вас нет прав на создание чатов");
        }

        // Проверяем, существует ли уже чат между этими пользователями
        Optional<Chat> existingChat = chatRepository.findPrivateChatBetween(userId1, userId2);
        if (existingChat.isPresent()) {
            return existingChat.get();
        }

        Chat chat = new Chat(ChatType.PRIVATE, user1);
        chatRepository.save(chat);

        ChatParticipant participant1 = new ChatParticipant(chat, user1, true);
        ChatParticipant participant2 = new ChatParticipant(chat, user2, false);

        chatParticipantRepository.save(participant1);
        chatParticipantRepository.save(participant2);

        return chat;
    }

    // Обновленный метод отправки сообщения
    @Transactional
    public Message sendMessage(Long chatId, Long authorId, String content) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Чат не найден"));

        User author = userService.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверяем права на отправку сообщений
        if (!canSendMessage(author, chat)) {
            throw new RuntimeException("У вас нет прав на отправку сообщений в этот чат");
        }

        Message message = new Message(chat, author, content);
        message = messageRepository.save(message);

        chat.setUpdatedAt(LocalDateTime.now());
        chatRepository.save(chat);

        return message;
    }
}