package com.den41k.service;

import com.den41k.model.Priority;
import com.den41k.model.Project;
import com.den41k.model.Task;
import com.den41k.model.TaskStatus;
import com.den41k.repository.ProjectRepository;
import com.den41k.repository.TaskRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public Task saveTask(Task task) {
        return taskRepository.merge(task);
    }

    @Transactional
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional
    public List<Task> findByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    @Transactional
    public void deleteTask(Long Id) {
        taskRepository.deleteById(Id);
    }

    @Transactional(readOnly = true)
    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    public void clearTaskCreator(Long userId) {
        taskRepository.clearCreatorReferences(userId);
    }

    public void clearTaskExecutor(Long userId) {
        taskRepository.clearExecutorReferences(userId);
    }

    public void clearApprover(Long userId) {
        List<Task> tasks = taskRepository.findByApproverId(userId);
        for (Task task : tasks) {
            task.setApprover(null);
            taskRepository.merge(task);
        }
    }

    public List<Task> findByProjectIdWithFilters(Long projectId, String search, String status, String priority, String executor) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            tasks = tasks.stream()
                    .filter(task -> task.getTitle() != null && task.getTitle().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty()) {
            try {
                TaskStatus taskStatus = TaskStatus.valueOf(status);
                tasks = tasks.stream()
                        .filter(task -> task.getTaskStatus() == taskStatus)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                return new ArrayList<>();
            }
        }

        if (priority != null && !priority.trim().isEmpty()) {
            try {
                Priority taskPriority = Priority.valueOf(priority);
                tasks = tasks.stream()
                        .filter(task -> task.getPriority() == taskPriority)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                return new ArrayList<>();
            }
        }

        if (executor != null && !executor.trim().isEmpty()) {
            if ("UNASSIGNED".equals(executor)) {
                tasks = tasks.stream()
                        .filter(task -> task.getTaskExecutor() == null)
                        .collect(Collectors.toList());
            } else {
                try {
                    Long executorId = Long.parseLong(executor);
                    tasks = tasks.stream()
                            .filter(task -> task.getTaskExecutor() != null && task.getTaskExecutor().getId().equals(executorId))
                            .collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    return new ArrayList<>();
                }
            }
        }

        return tasks;
    }

    public long countByProjectId(Long projectId) {
        return taskRepository.countByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<Task> findByApproverId(Long userId) {
        return taskRepository.findByApproverId(userId);
    }
}
