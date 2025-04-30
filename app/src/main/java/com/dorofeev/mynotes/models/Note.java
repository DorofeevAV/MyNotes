package com.dorofeev.mynotes.models;

import java.util.List;

/**
 * Модель заметки
 */
public class Note {
    private String id;                 // Firestore ID
    private String title;             // Заголовок
    private String contentMarkdown;   // Текст в Markdown
    private String groupId;           // ID группы
    private String imageUrl;          // Одно прикреплённое изображение
    private List<String> fileUrls;    // Ссылки на прикреплённые файлы
    private List<String> tags;        // Список тегов

    public Note() {
        // Конструктор без параметров (для Firestore)
    }

    /**
     * Конструктор с параметрами
     *
     * @param title           Заголовок заметки
     * @param contentMarkdown Содержимое заметки в формате Markdown
     * @param groupId         ID группы, к которой принадлежит заметка
     * @param imageUrl        URL изображения, прикреплённого к заметке
     * @param fileUrls        Список URL файлов, прикреплённых к заметке
     * @param tags            Список тегов, связанных с заметкой
     */
    public Note(String title, String contentMarkdown, String groupId, String imageUrl,
                List<String> fileUrls, List<String> tags ) {
        this.title = title;
        this.contentMarkdown = contentMarkdown;
        this.groupId = groupId;
        this.imageUrl = imageUrl;
        this.fileUrls = fileUrls;
        this.tags = tags;
    }

    // Геттер и сеттер для Id
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    // Геттер и сеттер для заголовка
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    // Геттер и сеттер для содержимого в Markdown
    public String getContentMarkdown() { return contentMarkdown; }
    public void setContentMarkdown(String contentMarkdown) { this.contentMarkdown = contentMarkdown; }
    // Геттер и сеттер для ID группы
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    // Геттер и сеттер для URL изображения
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    // Геттер и сеттер для списка URL файлов
    public List<String> getFileUrls() { return fileUrls; }
    public void setFileUrls(List<String> fileUrls) { this.fileUrls = fileUrls; }
    // Геттер и сеттер для списка тегов
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
