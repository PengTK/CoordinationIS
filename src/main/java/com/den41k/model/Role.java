package com.den41k.model;

public enum Role {
    CUSTOMER("Заказчик"),
    DEVELOPER("Разработчик"),
    TEAMLEAD("Тимлид"),
    MANAGER("Менеджер"),
    ADMIN("Администратор");

    final String name;

    Role(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
