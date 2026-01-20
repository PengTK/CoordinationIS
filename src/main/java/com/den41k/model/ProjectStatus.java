package com.den41k.model;

public enum ProjectStatus {
    PENDING("В обработке"),
    ACTIVE("В процессе"),
    PAUSED("На паузе"),
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
