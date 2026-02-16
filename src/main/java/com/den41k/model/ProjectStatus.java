package com.den41k.model;

public enum ProjectStatus {
    PENDING("Ожидает"),
    IN_PROGRESS("В работе"),
    COMPLETED("Заверщён"),
    CANCELLED("Отменён");

    final String name;

    ProjectStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
