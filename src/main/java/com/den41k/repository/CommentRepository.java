package com.den41k.repository;

import com.den41k.model.Comment;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(value = "SELECT c FROM Comment c WHERE c.task.id = :taskId ORDER BY c.createdAt DESC")
    List<Comment> findByTaskId(Long taskId);

    long countByTaskId(Long taskId);

    @Query(value = "UPDATE comments SET author_id = NULL WHERE author_id = :userId", nativeQuery = true)
    void clearAuthorReferences(@Parameter("userId") Long userId);
}