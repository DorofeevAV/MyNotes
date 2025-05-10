package com.dorofeev.mynotes.services;

import androidx.annotation.NonNull;

import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.models.Note;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Сервис для работы с заметками через Firestore
 */
public class NoteService {
    private final CollectionReference collectionRef; // Ссылка на коллекцию "notes" в Firestore
    private final ListenerRegistration notesListener; // Активный слушатель изменений Firestore
    // Кешированный список заметок
    private volatile List<Note> cachedNotes = new ArrayList<>();
    /*
     * Приватный конструктор — инициализация коллекции и запуск слушателя
     */
    public NoteService(final NotesChangedListener listener) {
        /* Получение коллекции "notes" из Firestore */
        this.collectionRef = FirebaseFirestore.getInstance().collection("notes");

        /* Установка слушателя Firestore, отслеживающего изменения коллекции */
        notesListener = collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                if (e != null || snapshots == null) {
                    if (listener != null) listener.onError(e);
                    return;
                }

                /* Список внутренних объектов с ID */
                List<Note> newList = new ArrayList<Note>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    DTO_Note data = doc.toObject(DTO_Note.class);
                    newList.add(data.toNote(doc.getId()));
                }
                // Кешируем список новых заметок
                cachedNotes = Collections.unmodifiableList(newList);
                // Вызов события
                if (listener != null) {
                    listener.onNotesChanged(cachedNotes);
                }
            }
        });
    }

    // callback интерфейс для подписки на изменения списка заметок
    public interface NotesChangedListener {
        void onNotesChanged(List<Note> notes); // Уведомление об обновлении
        void onError(Exception e);             // Уведомление об ошибке
    }

    // callback интерфейс для обратной связи после операций создания, обновления, удаления
    public interface OperationCallback {
        void onSuccess(Note note); // Операция успешна
        void onError(Exception e); // Ошибка
    }
    // callback интефйес уведомления об успешном удалении заметки
    public interface DeleteCallback {
        void onSuccess(List<String> noteIds); // Уведомление об успешном удалении
        void onError(Exception e);      // Уведомление об ошибке
    }

    /*
     * Получить кешированный список всех заметок
     * @return список заметок (неизменяемый)
     */
    public List<Note> getNotes() {
        return cachedNotes;
    }

    /*
     * Создать новую заметку в Firestore
     * @param title заголовок заметки
     * @param contentMarkdown текст заметки в формате Markdown
     * @param imageUrl URL изображения, прикреплённого к заметке
     * @param fileUrls список URL файлов, прикреплённых к заметке
     * @param tags список тегов, связанных с заметкой
     * @param callback колбэк для результата операции
     */
    public void createNote(@NonNull final String title, final String contentMarkdown, final String imageUrl,
                           final List<String> fileUrls, final List<String> tags,
                           @NonNull final OperationCallback callback) {
        final String id = collectionRef.document().getId(); // Генерация нового ID
        final Note note = new Note(id, title, contentMarkdown, imageUrl, fileUrls, tags);
        // Сохраниение новой заметки в Firestore
        collectionRef
                .document(id)
                .set(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onSuccess(note);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }


    /*
     * Обновить заметку в Firestore
     * @param note объект Note с новыми данными
     * @param callback колбэк для результата операции
     */
    public void updateNote(@NonNull Note note, @NonNull final OperationCallback callback) {
        collectionRef
                .document(note.getId())
                .set(new DTO_Note(note), SetOptions.merge()) // Обновление данных
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onSuccess(note);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /*
     * Удалить заметку из Firestore
     * @param note объект Note, подлежащий удалению
     * @param callback колбэк для результата операции
     */
    public void deleteNote(@NonNull String noteId, @NonNull final DeleteCallback callback) {
        deleteNotes(List.of(noteId),callback);
    }
    /*
     * Удалить список заметок из Firestore через batch-операцию
     * @param notes список заметок для удаления
     * @param callback обработчик завершения операции
     */
    public void deleteNotes(@NonNull List<String> notesIds, @NonNull final DeleteCallback callback) {
        if (notesIds.isEmpty()) {
            callback.onSuccess(notesIds);
            return;
        }

        WriteBatch batch = FirebaseFirestore.getInstance().batch();

        for (String noteId : notesIds) {
            batch.delete(collectionRef.document(noteId));
        }

        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onSuccess(notesIds);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }
    /*
     * Внутренний класс данных для Firestore
     */
    private static class DTO_Note {
        private String title;             // Заголовок
        private String contentMarkdown;   // Текст в Markdown
        private String imageUrl;          // Одно прикреплённое изображение
        private List<String> fileUrls;    // Ссылки на прикреплённые файлы
        private List<String> tags;        // Список тегов
        /*
         * Конструктор для создания пустого объекта для Firestore
         */
        public DTO_Note( ) {
        }
        /*
         * Конструктор, принимающий ID и объект Note
         * @param id идентификатор Firestore
         * @param note объект заметки
         */
        public DTO_Note(@NonNull Note note) {
            title = note.getTitle();
            contentMarkdown = note.getContentMarkdown();
            imageUrl = note.getImageUrl();
            fileUrls = note.getFileUrls();
            tags = note.getTags();
        }

        // Геттеры и сеттеры для Title
        public String getTitle( ) {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        // Геттеры и сеттеры для ContentMarkdown
        public String getContentMarkdown( ) {
            return contentMarkdown;
        }
        public void setContentMarkdown(String contentMarkdown) {
            this.contentMarkdown = contentMarkdown;
        }
        // Геттеры и сеттеры для ImageUrl
        public String getImageUrl( ) {
            return imageUrl;
        }
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        // Геттеры и сеттеры для FileUrls
        public List<String> getFileUrls( ) {
            return fileUrls;
        }
        public void setFileUrls(List<String> fileUrls) {
            this.fileUrls = fileUrls;
        }
        // Геттеры и сеттеры для Tags
        public List<String> getTags( ) {
            return tags;
        }
        public void setTags(List<String> tags) {
            this.tags = tags;
        }
        // Метод для получения объекта Group из объекта DTO_Note
        public Note toNote(@NonNull String id) {
            return new Note(id, title, contentMarkdown, imageUrl, fileUrls, tags);
        }
    }
}
