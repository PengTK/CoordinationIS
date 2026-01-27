package com.den41k.model;

public enum TaskStatus {
    TODO("В планах"),
    IN_PROGRESS("В процессе"),
    REVIEW("Пересматривается"),
    DONE("Заверщён");

    final String name;

    TaskStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
