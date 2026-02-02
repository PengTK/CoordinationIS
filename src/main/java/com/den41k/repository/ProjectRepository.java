package com.den41k.repository;

import com.den41k.model.Project;
import com.den41k.model.Task;
import io.micronaut.context.annotation.Parameter;
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
}
