package com.den41k.model;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Serdeable
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private TaskStatus taskStatus;

    @Column(nullable = false)
    private Priority priority;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = true)
    private User taskExecutor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = true)
    private User approver;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = true)
    private User taskCreator;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDate deadLine;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn()
    private Project project;

    public LocalDate getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(LocalDate deadLine) {
        this.deadLine = deadLine;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getTaskCreator() {
        return taskCreator;
    }

    public void setTaskCreator(User taskCreator) {
        this.taskCreator = taskCreator;
    }

    public User getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(User taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    public Task(String title, String description, TaskStatus taskStatus, Priority priority,
                User taskExecutor, User taskCreator, User approver, LocalDate deadLine, Project project) {
        this.title = title;
        this.description = description;
        this.taskStatus = taskStatus;
        this.priority = priority;
        this.taskExecutor = taskExecutor;
        this.taskCreator = taskCreator;
        this.approver = approver;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deadLine = deadLine;
        this.project = project;
    }


    public Task() {
    }
}
