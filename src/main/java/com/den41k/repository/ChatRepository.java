package com.den41k.repository;

import com.den41k.model.Chat;
import com.den41k.model.ChatType;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    @Query("SELECT c FROM Chat c JOIN ChatParticipant cp ON c.id = cp.chat.id WHERE cp.user.id = :userId ORDER BY c.updatedAt DESC")
    List<Chat> findByUserId(@Parameter("userId") Long userId);
    
    @Query("SELECT c FROM Chat c WHERE c.type = :type AND c.project.id = :projectId")
    Optional<Chat> findProjectChat(@Parameter("type") ChatType type, @Parameter("projectId") Long projectId);
    
    @Query("SELECT c FROM Chat c JOIN ChatParticipant cp1 ON c.id = cp1.chat.id JOIN ChatParticipant cp2 ON c.id = cp2.chat.id WHERE c.type = 'PRIVATE' AND cp1.user.id = :userId1 AND cp2.user.id = :userId2")
    Optional<Chat> findPrivateChatBetween(@Parameter("userId1") Long userId1, @Parameter("userId2") Long userId2);
    
    @Query("SELECT COUNT(c) FROM Chat c JOIN ChatParticipant cp ON c.id = cp.chat.id WHERE cp.user.id = :userId")
    long countByUserId(@Parameter("userId") Long userId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.createdAt > :since")
    long countUnreadMessages(@Parameter("chatId") Long chatId, @Parameter("since") java.time.LocalDateTime since);
}