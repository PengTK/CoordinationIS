package com.den41k.repository;

import com.den41k.model.Project;
import com.den41k.model.Task;
import com.den41k.model.User;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAll();

    @Query("SELECT p FROM Project p WHERE p.projectCreator.id = :userId")
    List<Project> findByProjectCreatorId(@Parameter("userId") Long userId);

    @Query("SELECT p FROM Project p WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR :status = '' OR p.projectStatus = com.den41k.model.ProjectStatus.valueOf(:status)) " +
            "AND (:creatorId IS NULL OR p.projectCreator.id = :creatorId) " +
            "ORDER BY p.createdAt DESC")
    List<Project> searchProjects(@Parameter("search") String search,
                                 @Parameter("status") String status,
                                 @Parameter("creatorId") @Nullable Long creatorId);

    @Query("SELECT DISTINCT p.projectCreator FROM Project p WHERE p.projectCreator IS NOT NULL ORDER BY p.projectCreator.name")
    List<User> findAllProjectCreators();
}
