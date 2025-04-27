package com.dorofeev.mynotes.models;

// Модель для хранения информации о пользователе
public class User {
    // Идентификатор пользователя
    private String id;
    // Имя пользователя
    private String username;
    // Конструктор без параметров
    public User() {
    }
    // Конструктор с параметрами id - идентификатор пользователя, username - имя пользователяё
    public User(String id, String username) {
        this.id = id;
        this.username = username;
    }
    // Id - геттер
    public String getId() {
        return id;
    }
    // Id - сеттер
    public void setId(String id) {
        this.id = id;
    }
    // Имя пользователя - геттер
    public String getUsername() {
        return username;
    }
    // Имя пользователя - сеттер
    public void setUsername(String username) {
        this.username = username;
    }
}