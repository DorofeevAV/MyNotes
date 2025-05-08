package com.dorofeev.mynotes.models;

import androidx.annotation.NonNull;

/*
 * Модель данных для связи между группой и заметкой
 */
public class GroupNoteLink {
    private final String Id; // Идентификатор группы
    private String groupId; // Идентификатор заметки
    private String noteId; // Идентификатор заметки
    public GroupNoteLink(@NonNull String id, @NonNull String groupId, @NonNull String noteId) {
        this.Id = id;
        this.groupId = groupId;
        this.noteId = noteId;
    }
    // Геттер идентификатора
    public String getId() {
        return Id;
    }
    // Геттер и сеттер для groupId
    public String getGroupId() {
        return groupId;
    }
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    // Геттер и сеттер для noteId
    public String getNoteId() {
        return noteId;
    }
    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }
}
