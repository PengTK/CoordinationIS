package com.den41k.repository;

import com.den41k.model.Message;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.createdAt DESC LIMIT :limit")
    List<Message> findLastMessages(@Parameter("chatId") Long chatId, @Parameter("limit") int limit);
    
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.createdAt > :since ORDER BY m.createdAt ASC")
    List<Message> findMessagesSince(@Parameter("chatId") Long chatId, @Parameter("since") java.time.LocalDateTime since);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId")
    long countByChatId(@Parameter("chatId") Long chatId);
}