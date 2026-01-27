package com.den41k.model;

public enum Priority {
    LOW("Низкий"),
    MEDIUM("Средний"),
    HIGH("Высокий"),
    URGENT("Срочный");

    final String name;

    Priority(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
