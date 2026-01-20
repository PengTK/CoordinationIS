package com.den41k.repository;

import com.den41k.model.Project;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {



}
