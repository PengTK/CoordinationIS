package com.den41k.repository;

import com.den41k.model.ChatParticipant;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    
    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.chat.id = :chatId AND cp.user.id = :userId")
    Optional<ChatParticipant> findByChatIdAndUserId(@Parameter("chatId") Long chatId, @Parameter("userId") Long userId);
    
    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.chat.id = :chatId")
    List<ChatParticipant> findByChatId(@Parameter("chatId") Long chatId);
    
    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.user.id = :userId")
    List<ChatParticipant> findByUserId(@Parameter("userId") Long userId);
    
    @Query("SELECT COUNT(cp) FROM ChatParticipant cp WHERE cp.chat.id = :chatId")
    long countByChatId(@Parameter("chatId") Long chatId);
    
    @Query("DELETE FROM ChatParticipant cp WHERE cp.user.id = :userId")
    void deleteByUserId(@Parameter("userId") Long userId);
}