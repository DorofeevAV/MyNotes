package com.dorofeev.mynotes.models;

/**
 * Модель данных для группы заметок
 */
public class Group {
    // Уникальный идентификатор группы (Firestore ID)
    private String id;

    // Название группы (например: "Учеба", "Работа", "Личное")
    private String name;

    /**
     * Конструктор без параметров (для работы с Firebase)
     */
    public Group() {
    }

    /**
     * Конструктор для создания объекта с начальными значениями
     * @param id Идентификатор группы
     * @param name Название группы
     */
    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }
    //
    // Геттер для id
    public String getId() {
        return id;
    }

    // Сеттер для id
    public void setId(String id) {
        this.id = id;
    }

    // Геттер для name
    public String getName() {
        return name;
    }

    // Сеттер для name
    public void setName(String name) {
        this.name = name;
    }
}
