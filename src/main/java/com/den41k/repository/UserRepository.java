package com.den41k.repository;

import com.den41k.model.User;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("""
        SELECT u FROM User u
        WHERE (:query IS NULL OR LOWER(u.name) LIKE :query OR LOWER(u.sureName) LIKE :query OR LOWER(u.email) LIKE :query)
          AND (:roleId IS NULL OR u.role.id = :roleId)
        ORDER BY u.createdAt DESC
        """)
    List<User> search(@Nullable String query, @Nullable Long roleId);

    @Query("""
        SELECT u FROM User u
        WHERE (:query IS NULL OR LOWER(u.name) LIKE :query OR LOWER(u.sureName) LIKE :query OR LOWER(u.email) LIKE :query)
          AND (:roleId IS NULL OR u.role.id = :roleId)
        ORDER BY u.createdAt ASC
        """)
    List<User> searchOldestFirst(@Nullable String query, @Nullable Long roleId);

    long countByRoleId(Long roleId);
}
