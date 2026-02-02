package com.den41k.service;

import com.den41k.model.Project;
import com.den41k.repository.ProjectRepository;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

@Singleton
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public Project saveProject(Project project) {
        return projectRepository.merge(project);
    }

    @Transactional
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Transactional
    public @NonNull Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public void clearProjectCreator(Long userId) {
        List<Project> projects = projectRepository.findByProjectCreatorId(userId);
        for (Project project : projects) {
            project.setProjectCreator(null);
            projectRepository.merge(project);
        }
    }

    public List<Project> findByProjectCreatorId(Long userId) {
        return projectRepository.findByProjectCreatorId(userId);
    }
}
