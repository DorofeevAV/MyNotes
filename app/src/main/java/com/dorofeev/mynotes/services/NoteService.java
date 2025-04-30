package com.dorofeev.mynotes.services;

import androidx.annotation.NonNull;

import com.dorofeev.mynotes.models.Note;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Сервис для работы с заметками (создание, обновление, удаление, прослушка, поиск)
 */
public class NoteService {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference notesRef = db.collection("notes");
    private ListenerRegistration notesListener;

    /**
     * Callback для слушателей изменений списка заметок
     */
    public interface NotesChangedListener {
        void onNotesChanged(List<Note> notes);
        void onError(Exception e);
    }

    /**
     * Callback для операций создания, удаления и обновления
     */
    public interface OperationCallback {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * Создание новой заметки в коллекции Firestore
     */
    public void createNote(@NonNull Note note, @NonNull final OperationCallback callback) {
        notesRef.add(note)
                .addOnSuccessListener(docRef -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Обновление существующей заметки
     */
    public void updateNote(@NonNull Note note, @NonNull final OperationCallback callback) {
        if (note.getId() == null) {
            callback.onError(new IllegalArgumentException("Note ID is null"));
            return;
        }

        notesRef.document(note.getId())
                .set(note)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Удаление заметки по ID
     */
    public void deleteNote(@NonNull String noteId, @NonNull final OperationCallback callback) {
        notesRef.document(noteId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Подписка на изменения списка заметок в рамках одной группы
     * @param groupId ID группы, к которой привязаны заметки
     */
    public void startListeningNotesInGroup(@NonNull String groupId, @NonNull final NotesChangedListener listener) {
        notesListener = notesRef
                .whereEqualTo("groupId", groupId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            listener.onError(e);
                            return;
                        }

                        List<Note> notes = new ArrayList<>();
                        if (snapshots != null) {
                            for (QueryDocumentSnapshot doc : snapshots) {
                                Note note = doc.toObject(Note.class);
                                note.setId(doc.getId()); // важно для Firestore ID
                                notes.add(note);
                            }
                        }

                        listener.onNotesChanged(notes);
                    }
                });
    }

    /**
     * Остановка прослушивания изменений заметок
     */
    public void stopListeningNotes() {
        if (notesListener != null) {
            notesListener.remove();
            notesListener = null;
        }
    }

    /**
     * Поиск заметок по заголовку или тегам (локальный фильтр)
     * @param query Поисковый текст
     */
    public void searchNotes(@NonNull String query, @NonNull final NotesChangedListener callback) {
        notesRef.get()
                .addOnSuccessListener(snapshot -> {
                    List<Note> results = new ArrayList<>();
                    String lowerQuery = query.toLowerCase();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Note note = doc.toObject(Note.class);
                        note.setId(doc.getId());

                        boolean matchesTitle = false;
                        boolean matchesTags = false;

                        if (note.getTitle() != null &&
                                note.getTitle().toLowerCase().contains(lowerQuery)) {
                            matchesTitle = true;
                        }

                        if (note.getTags() != null) {
                            for (String tag : note.getTags()) {
                                if (tag != null && tag.toLowerCase().contains(lowerQuery)) {
                                    matchesTags = true;
                                    break;
                                }
                            }
                        }

                        if (matchesTitle || matchesTags) {
                            results.add(note);
                        }
                    }

                    callback.onNotesChanged(results);
                })
                .addOnFailureListener(callback::onError);
    }
}
