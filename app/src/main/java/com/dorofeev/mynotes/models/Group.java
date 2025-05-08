package com.dorofeev.mynotes.models;

import androidx.annotation.NonNull;

/*
 * Модель данных для группы заметок
 */
public class Group {
    // Идентификатор группы
    private final String id;
    // Название группы
    private String name;
    /*
     * Конструктор для создания объекта с начальными значениями
     * @param id Идентификатор пользователя - берется из БД
     * @param dto Объект данных из Firebase
     */
    public Group(@NonNull String id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }
    /*
     * Конструктор копии объекта с начальными значениями
     * @param group объект для копии
     */
    public Group(@NonNull Group group) {
        id = group.id;
        name = group.name;
    }
    // Геттер идентификатора
    public String getId() {
        return id;
    }
    // Геттер и сеттер для name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
