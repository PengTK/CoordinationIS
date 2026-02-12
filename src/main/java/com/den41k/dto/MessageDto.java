package com.den41k.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.time.LocalDateTime;

@Serdeable
public class MessageDto {
    private Long id;
    private Long chatId;
    private Long authorId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;

    public MessageDto() {}

    public MessageDto(Long id, Long chatId, Long authorId, String authorName, String content, LocalDateTime createdAt) {
        this.id = id;
        this.chatId = chatId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}