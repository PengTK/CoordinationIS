package com.den41k.repository;

import com.den41k.model.Message;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.createdAt > :since ORDER BY m.createdAt ASC")
    List<Message> findMessagesSince(@Parameter("chatId") Long chatId, @Parameter("since") LocalDateTime since);

    // Для загрузки всех сообщений чата
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.createdAt ASC")
    List<Message> findAllByChatId(@Parameter("chatId") Long chatId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId")
    long countByChatId(@Parameter("chatId") Long chatId);

    @Query("DELETE FROM Message m WHERE m.author.id = :userId")
    void deleteByUserId(@Parameter("userId") Long userId);
}