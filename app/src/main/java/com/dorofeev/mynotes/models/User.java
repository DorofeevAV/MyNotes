package com.dorofeev.mynotes.models;

// Модель для хранения информации о пользователе
public class User {
    // Идентификатор пользователя
    private final String id;
    // Имя пользователя
    private final String username;
    /*
     * Конструктор для создания объекта с начальными значениями
     * @param id Идентификатор пользователя - берется из БД
     * @param dto Объект данных из Firebase
     */
    public User(String id, UserDTO dto) {
        this.id = id;
        this.username = dto.username;
    }
    // Id - геттер
    public String getId() {
        return id;
    }
    // Имя пользователя - геттер
    public String getUsername() {
        return username;
    }
    // Объект данных для Firebase
    public static class UserDTO {
        // Имя пользователя
        private String username;
        public UserDTO() {
            // Пустой конструктор для Firebase
        }
        // Конструктор для отладки
        public UserDTO(String user) {
            this.username = user;
        }
        // getter и setter для имени пользователя
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
    }
}