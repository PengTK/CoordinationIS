package com.den41k.repository;

import com.den41k.model.Priority;
import com.den41k.model.Task;
import com.den41k.model.TaskStatus;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId")
    List<Task> findByProjectId(@Parameter("projectId") Long projectId);

    @Query(value = "UPDATE tasks SET task_creator_id = NULL WHERE task_creator_id = :userId", nativeQuery = true)
    void clearCreatorReferences(@Parameter("userId") Long userId);

    @Query(value = "UPDATE tasks SET task_executor_id = NULL WHERE task_executor_id = :userId", nativeQuery = true)
    void clearExecutorReferences(@Parameter("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    long countByProjectId(@Parameter("projectId") Long projectId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Task> findByProjectIdAndTitleContaining(@Parameter("projectId") Long projectId, @Parameter("search") String search);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.taskStatus = :status")
    List<Task> findByProjectIdAndTaskStatus(@Parameter("projectId") Long projectId, @Parameter("status")
    TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.priority = :priority")
    List<Task> findByProjectIdAndPriority(@Parameter("projectId") Long projectId, @Parameter("priority") Priority priority);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.taskExecutor IS NULL")
    List<Task> findByProjectIdAndUnassigned(@Parameter("projectId") Long projectId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.taskExecutor.id = :executorId")
    List<Task> findByProjectIdAndExecutor(@Parameter("projectId") Long projectId, @Parameter("executorId") Long executorId);

    @Query("SELECT t FROM Task t WHERE t.approver.id = :userId")
    List<Task> findByApproverId(Long userId);
}
