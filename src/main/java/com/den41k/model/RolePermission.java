package com.den41k.model;

public enum RolePermission {
    ALL_PERMS("Полный доступ"),
    READ_ONLY("Только чтение"),
    NO_PERMS("Нет доступа");


    final String name;

    RolePermission(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
