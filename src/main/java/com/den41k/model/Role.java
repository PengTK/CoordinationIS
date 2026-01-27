package com.den41k.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private RolePermission projectPermission;

    @Column(nullable = false)
    private RolePermission taskPermission;

    @Column(nullable = false)
    private RolePermission rolePermission;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RolePermission getProjectPermission() {
        return projectPermission;
    }

    public void setProjectPermission(RolePermission projectPermission) {
        this.projectPermission = projectPermission;
    }

    public RolePermission getTaskPermission() {
        return taskPermission;
    }

    public void setTaskPermission(RolePermission taskPermission) {
        this.taskPermission = taskPermission;
    }

    public RolePermission getRolePermission() {
        return rolePermission;
    }

    public void setRolePermission(RolePermission rolePermission) {
        this.rolePermission = rolePermission;
    }

    public Role() {
    }

    public Role(String name, RolePermission projectPermission, RolePermission taskPermission, RolePermission rolePermission) {
        this.name = name;
        this.projectPermission = projectPermission;
        this.taskPermission = taskPermission;
        this.rolePermission = rolePermission;
    }
}
