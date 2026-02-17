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

    public String getLabel() {
        return switch (this) {
            case TODO -> "К выполнению";
            case IN_PROGRESS -> "В работе";
            case REVIEW -> "На проверке";
            case DONE -> "Завершено";
        };
    }
}
