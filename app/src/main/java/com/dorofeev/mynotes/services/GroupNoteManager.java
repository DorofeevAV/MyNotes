package com.dorofeev.mynotes.services;

import androidx.annotation.NonNull;

import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.models.GroupNoteLink;
import com.dorofeev.mynotes.models.Note;

import java.util.*;

/*
 * Менеджер заметок и групп — построение структуры дерева заметок
 * Весь UI будет работать через него
 */
public class GroupNoteManager {
    private static GroupNoteManager instance; // Экземпляр менеджера
    private final GroupService groupService; // Сервис для работы с группами
    private final NoteService noteService;  // Сервис для работы с заметками
    private final GroupNoteLinkService linkService; // Сервис для работы со связями
    private final Map<String, Group> groupsById = new HashMap<>();  // Кэш групп
    private final Map<String, Note> notesById = new HashMap<>();  // Кэш заметок
    private final Map<String, List<Note>> groupIdToNotes = new HashMap<>(); // Кэш групп => заметки
    private StructureChangedCallback structureChangedCallback; // Обратный вызов для изменения структуры
    /*
     * Закрытый конструктор
     */
    private GroupNoteManager() {
        // Подписываемся на изменение групп
        groupService = new GroupService(new GroupService.GroupsChangedListener() {
            @Override
            public void onGroupsChanged(List<Group> groups) {
                groupsById.clear();
                for (Group g : groups) {
                    groupsById.put(g.getId(), g);
                }
                rebuildStructure();
            }

            @Override
            public void onError(Exception e) {
                if (structureChangedCallback != null) {
                    structureChangedCallback.onError(e);
                }
            }
        });
        // Подписываемся на изменение заметок
        noteService = new NoteService(new NoteService.NotesChangedListener() {
            @Override
            public void onNotesChanged(List<Note> notes) {
                notesById.clear();
                for (Note n : notes) {
                    notesById.put(n.getId(), n);
                }
                rebuildStructure();
            }

            @Override
            public void onError(Exception e) {
                if (structureChangedCallback != null) {
                    structureChangedCallback.onError(e);
                }
            }
        });
        // Подписываемся на изменение связей
        linkService = new GroupNoteLinkService(new GroupNoteLinkService.LinksChangedListener() {
            @Override
            public void onLinksChanged(List<GroupNoteLink> links) {
                rebuildStructure();
            }

            @Override
            public void onError(Exception e) {
                if (structureChangedCallback != null) {
                    structureChangedCallback.onError(e);
                }
            }
        });
    }
    // Получение единственного экземпляра менеджера
    public static synchronized GroupNoteManager getInstance() {
        if (instance == null) {
            instance = new GroupNoteManager(); // Создаем единственный экземпляр
        }
        return instance;
    }
     // callback интерфейс для обратной связи о изменении структуры групп
    public interface StructureChangedCallback {
        void onStructureChanged(Map<Group, List<Note>> currentStructure);
        void onError(Exception e);
    }

    /*
     * Установка обратного вызова для изменения структуры
     * @param callback Обратный вызов
     */
    public void setStructureChangedCallback(StructureChangedCallback callback) {
        this.structureChangedCallback = callback;
        if (structureChangedCallback != null && !groupIdToNotes.isEmpty()) {
            structureChangedCallback.onStructureChanged(getStructureSnapshot());
        }
    }
    /*
     * Получить текущую структуру групп и заметок
     * @return Текущая структура групп и заметок
     */
    public Map<Group,List<Note>> getStructureSnapshot() {
        Map<Group, List<Note>> snapshot = new HashMap<>();
        for (Map.Entry<String, List<Note>> entry : groupIdToNotes.entrySet()) {
            Group group = groupsById.get(entry.getKey());
            if (group != null) {
                snapshot.put(group, new ArrayList<>(entry.getValue()));
            }
        }
        return snapshot;
    }
    /*
     * Создать группу
     * @param name Имя группы
     * @param callback Обратный вызов
     */
    public void createGroup(String name, GroupService.OperationCallback callback) {
        groupService.createGroup(name, callback);
    }
    /*
     * Обновить группу
     * @param group Группа с обновлённым именем
     * @param callback Обратный вызов
     */
    public void updateGroup(@NonNull Group group, @NonNull GroupService.OperationCallback callback) {
        groupService.updateGroup(group, callback);
    }
     /*
     * Создать заметку и связать её с группой
     * @param group Группа, с которой будет связана заметка
     * @param title Заголовок заметки
     * @param content Содержимое заметки
     * @param imageUrl URL изображения
     * @param files Список файлов
     * @param tags Список тегов
     * @param callback Обратный вызов
     */
    public void createNote(@NonNull Group group, @NonNull String title, String content,
                           String imageUrl, List<String> files, List<String> tags,
                           NoteService.OperationCallback callback) {
        noteService.createNote(title, content, imageUrl, files, tags, new NoteService.OperationCallback() {
            @Override
            public void onSuccess(Note note) {
                linkService.createLink(group.getId(), note.getId(), new GroupNoteLinkService.OperationCallback() {
                    @Override
                    public void onSuccess(GroupNoteLink link) {
                        callback.onSuccess(note);
                    }
                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                });
            }
            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
    /*
     * Обновить заметку и её связь с группой
     * @param note Заметка, которую нужно обновить
     * @param newGroup Новая группа (если null) то группа не меняется
     * @param callback Обратный вызов
     */
    public void updateNote(@NonNull final Note note, final Group newGroup, @NonNull final NoteService.OperationCallback callback) {
        noteService.updateNote(note, new NoteService.OperationCallback() {
            @Override
            public void onSuccess(Note updatedNote) {
                if (newGroup == null) {
                    callback.onSuccess(updatedNote);
                    return;
                }

                for (GroupNoteLink link : linkService.getLinks()) {
                    if (link.getNoteId().equals(updatedNote.getId())) {
                        if (!link.getGroupId().equals(newGroup.getId())) {
                            link.setGroupId(newGroup.getId());
                            linkService.updateLink(link, new GroupNoteLinkService.OperationCallback() {
                                @Override
                                public void onSuccess(GroupNoteLink l) {
                                    callback.onSuccess(updatedNote);
                                }

                                @Override
                                public void onError(Exception e) {
                                    callback.onError(e);
                                }
                            });
                            return;
                        } else {
                            break; // Группа не изменилась
                        }
                    }
                }

                // Если группа та же или связь не найдена — просто успех
                callback.onSuccess(updatedNote);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
    /*
     * Удалить заметку
     * @param note Заметка, которую нужно удалить
     * @param callback Обратный вызов
     */
    public void deleteNote(@NonNull String noteId, NoteService.DeleteCallback callback) {
        List<String> toDelete = new ArrayList();
        for (GroupNoteLink link : linkService.getLinks()) {
            if (link.getNoteId().equals(noteId)) {
                toDelete.add(link.getId());
            }
        }
        // Сначала удаляем связи
        linkService.deleteLinks(toDelete, new GroupNoteLinkService.DeleteCallback() {
            @Override
            public void onSuccess(List<String> linksIds) {
                noteService.deleteNote(noteId, callback);
            }
            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
    /*
     * Удалить группу и все связанные с ней заметки
     * @param group Группа, которую нужно удалить
     * @param callback Обратный вызов
     */
    public void deleteGroup(@NonNull String groupId, @NonNull GroupService.DeleteCallback callback) {
        List<String> noteIds = new ArrayList<>();
        List<String> linksIds = new ArrayList<>();
        //
        for (GroupNoteLink link : linkService.getLinks()) {
            if (link.getGroupId().equals(groupId)) {
                linksIds.add(link.getId());
                noteIds.add(link.getNoteId());
            }
        }
        // Удаляем сначала связи
        linkService.deleteLinks(linksIds, new GroupNoteLinkService.DeleteCallback() {
            @Override
            public void onSuccess(List<String> linksIds) {
                // Удаляем заметки
                noteService.deleteNotes(noteIds, new NoteService.DeleteCallback() {
                    @Override
                    public void onSuccess(List<String> noteIds) {
                        // Удаляем группу
                        groupService.deleteGroup(groupId,callback);
                    }
                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                });
            }
            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    // Перестроение структуры групп -> заметки
    private void rebuildStructure() {
        // Если нет групп
        if (groupsById.isEmpty())
            return;

        groupIdToNotes.clear();

        // Сначала добавим все группы как ключи с пустыми списками
        for (Map.Entry<String, Group> entry : groupsById.entrySet()) {
            groupIdToNotes.put(entry.getKey(), new ArrayList<>());
        }

        // Если есть заметки и связи, добавим их
        if (!notesById.isEmpty() && !linkService.getLinks().isEmpty()) {
            for (GroupNoteLink link : linkService.getLinks()) {
                String groupId = link.getGroupId();
                String noteId = link.getNoteId();
                Group group = groupsById.get(groupId);
                Note note = notesById.get(noteId);
                if (group != null && note != null) {
                    groupIdToNotes.get(groupId).add(note);
                }
            }
        }
        // Если есть обратный вызов, вызываем его
        if (structureChangedCallback != null) {
            Map<Group, List<Note>> snapshot = new HashMap<>();
            for (Map.Entry<String, List<Note>> entry : groupIdToNotes.entrySet()) {
                Group group = groupsById.get(entry.getKey());
                if (group != null) {
                    snapshot.put(group, new ArrayList<>(entry.getValue()));
                }
            }
            structureChangedCallback.onStructureChanged(snapshot);
        }
    }
}
