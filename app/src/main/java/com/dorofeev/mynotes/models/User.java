package com.dorofeev.mynotes.models;

// Модель для хранения информации о пользователе
public class User {
    private final String id;          // Идентификатор пользователя
    private final String username;    // Имя пользователя
    /*
     * Конструктор для создания объекта с начальными значениями
     * @param id - идентификатор пользователя
     * @param username - имя пользователя
     */
    public User(String id, String username) {
        this.id = id;
        this.username = username;
    }
    // Id - геттер
    public String getId() {
        return id;
    }
    // Имя пользователя - геттер
    public String getUsername() {
        return username;
    }
}