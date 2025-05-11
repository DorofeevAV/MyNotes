package com.dorofeev.mynotes.models;

import androidx.annotation.NonNull;
import java.util.List;
import java.io.Serializable;

/*
 * Модель заметки (без Firestore ID)
 */
public class Note implements Serializable {
    private final String id;                // Идентификатор заметки
    private String title;             // Заголовок
    private String contentMarkdown;   // Текст в Markdown
    private String imageUrl;          // Одно прикреплённое изображение
    private List<String> fileUrls;    // Ссылки на прикреплённые файлы
    private List<String> tags;        // Список тегов

    /*
     * Конструктор для создания объекта с начальными значениями
     * @param id              Идентификатор заметки
     * @param title           Заголовок заметки
     * @param contentMarkdown Содержимое заметки в формате Markdown
     * @param imageUrl        URL изображения, прикреплённого к заметке
     * @param fileUrls        Список URL файлов, прикреплённых к заметке
     * @param tags            Список тегов, связанных с заметкой
     */
    public Note(@NonNull String id, @NonNull String title, String contentMarkdown, String imageUrl,
                List<String> fileUrls, List<String> tags) {
        this.id = id;
        this.title = title;
        this.contentMarkdown = contentMarkdown;
        this.imageUrl = imageUrl;
        this.fileUrls = fileUrls;
        this.tags = tags;
    }
    /*
     * Конструктор копии объекта
     * @param note объект для копии
     */
    public Note( @NonNull Note note ){
        this.id = note.id;
        this.title = note.title;
        this.contentMarkdown = note.contentMarkdown;
        this.imageUrl = note.imageUrl;
        this.fileUrls = note.fileUrls;
        this.tags = note.tags;
    }

    // Геттер для id
    public String getId( ) {
        return id;
    }

    // Геттер и сеттер для заголовка
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    // Геттер и сеттер для содержимого в Markdown
    public String getContentMarkdown() {
        return contentMarkdown;
    }
    public void setContentMarkdown(String contentMarkdown) {
        this.contentMarkdown = contentMarkdown;
    }

    // Геттер и сеттер для URL изображения
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Геттер и сеттер для списка URL файлов
    public List<String> getFileUrls() {
        return fileUrls;
    }
    public void setFileUrls(List<String> fileUrls) {
        this.fileUrls = fileUrls;
    }

    // Геттер и сеттер для списка тегов
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
