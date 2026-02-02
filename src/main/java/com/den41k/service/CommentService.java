package com.den41k.service;

import com.den41k.model.Comment;
import com.den41k.model.Task;
import com.den41k.model.User;
import com.den41k.repository.CommentRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Comment addComment(String content, Task task, User author) {
        Comment comment = new Comment(content, task, author);
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByTaskId(Long taskId) {
        return commentRepository.findByTaskId(taskId);
    }

    @Transactional(readOnly = true)
    public long countCommentsByTaskId(Long taskId) {
        return commentRepository.countByTaskId(taskId);
    }

    @Transactional
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    public void clearCommentAuthor(Long userId) {
        commentRepository.clearAuthorReferences(userId);
    }
}