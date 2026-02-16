package com.den41k.service;

import com.den41k.model.Project;
import com.den41k.model.ProjectStatus;
import com.den41k.model.User;
import com.den41k.repository.ProjectRepository;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Sort;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<Project> searchProjects(String search, String status, String creator) {
        List<Project> allProjects = projectRepository.findAll(Sort.of(Sort.Order.desc("createdAt")));
        List<Project> filtered = new ArrayList<>(allProjects);

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(p -> (p.getTitle() != null && p.getTitle().toLowerCase().contains(searchLower)) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty()) {
            try {
                ProjectStatus statusEnum = ProjectStatus.valueOf(status);
                filtered = filtered.stream()
                        .filter(p -> p.getProjectStatus() == statusEnum)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                return new ArrayList<>();
            }
        }

        if (creator != null && !creator.trim().isEmpty()) {
            try {
                Long creatorId = Long.parseLong(creator);
                filtered = filtered.stream()
                        .filter(p -> p.getProjectCreator() != null && p.getProjectCreator().getId().equals(creatorId))
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                return new ArrayList<>();
            }
        }

        return filtered;
    }

    public List<User> getAllProjectCreators() {
        return projectRepository.findAllProjectCreators();
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll(Sort.of(Sort.Order.desc("createdAt")));
    }
}
