package com.den41k.service;

import com.den41k.model.Project;
import com.den41k.model.User;
import com.den41k.repository.ProjectRepository;
import com.den41k.repository.UserRepository;
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
        return projectRepository.save(project);
    }

    @Transactional
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
}
