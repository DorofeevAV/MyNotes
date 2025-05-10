package com.dorofeev.mynotes.services;

import androidx.annotation.NonNull;

import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.models.GroupNoteLink;
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
 * Сервис для работы со связями "группа <-> заметка" через Firestore
 */
public class GroupNoteLinkService {

    private final CollectionReference collectionRef;
    private final ListenerRegistration listener;
    private volatile List<GroupNoteLink> cachedLinks = new ArrayList<>();
    // callback интерфейс для уведомления об изменениях
    public interface LinksChangedListener {
        void onLinksChanged(List<GroupNoteLink> links);
        void onError(Exception e);
    }
    // callback интерфейс для обратного вызова операций
    public interface OperationCallback {
        void onSuccess(GroupNoteLink link);
        void onError(Exception e);
    }
    // callback интерфейс для удаления ссылок
    public interface DeleteCallback {
        void onSuccess(List<String> linksIds);
        void onError(Exception e);
    }

    /*
     * Конструктор для создания объекта с начальными значениями
     * @param listenerCallback Слушатель изменений
     */
    public GroupNoteLinkService(final LinksChangedListener listenerCallback) {
        this.collectionRef = FirebaseFirestore.getInstance().collection("group_note_links");

        this.listener = collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(com.google.firebase.firestore.QuerySnapshot snapshots, FirebaseFirestoreException e) {
                if (e != null || snapshots == null) {
                    if (listenerCallback != null) listenerCallback.onError(e);
                    return;
                }

                List<GroupNoteLink> newList = new ArrayList<GroupNoteLink>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    DTO_Link dto = doc.toObject(DTO_Link.class);
                    if (dto.groupId != null && dto.noteId != null) {
                        newList.add(dto.toGroup(doc.getId()));
                    }
                }

                cachedLinks = Collections.unmodifiableList(newList);
                if (listenerCallback != null) listenerCallback.onLinksChanged(cachedLinks);
            }
        });
    }

    /*
     * Получить спислк ссылое
     */
    public List<GroupNoteLink> getLinks() {
        return cachedLinks;
    }
    /*
     * Создать ссылку между группой и заметкой
     * @param groupId Идентификатор группы
     * @param noteId Идентификатор заметки
     */
    public void createLink(@NonNull final String groupId, @NonNull final String noteId, @NonNull final OperationCallback callback) {
        final String id = collectionRef.document().getId();
        final GroupNoteLink link = new GroupNoteLink(id, groupId, noteId);

        collectionRef
                .document()
                .set(new DTO_Link(link))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onSuccess(link);
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
     * Обновить ссылку между группой и заметкой
     * @param link Ссылка для обновления
     * @param callback Callback для обработки результата
     */
    public void updateLink(@NonNull final GroupNoteLink link, @NonNull final OperationCallback callback) {

        DTO_Link dto = new DTO_Link(link);
        collectionRef
                .document(link.getId())
                .set(dto, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onSuccess(link);
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
     * Удалить одну или несколько ссылок между группами и заметками через Batch
     * @param links Список ссылок для удаления
     * @param callback Callback для обработки результата
     */
    public void deleteLinks(@NonNull final List<String> linksIds, @NonNull final DeleteCallback callback) {
        if (linksIds.isEmpty()) {
            callback.onSuccess(linksIds); // ничего удалять
            return;
        }

        WriteBatch batch = FirebaseFirestore.getInstance().batch();

        for (String linkId : linksIds) {
            batch.delete(collectionRef.document(linkId));
        }

        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // можно передать первый как результат
                        callback.onSuccess(linksIds);
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
     * Перегрузка для удаления одной ссылки
     * @param link Ссылка для удаления
     * @param callback Callback для обработки результата
     */
    public void deleteLink(@NonNull String linkId, @NonNull final DeleteCallback callback) {
        deleteLinks(List.of(linkId), callback);
    }

    /*
     * DTO класс для хранения данных в Firestore
     */
    private static class DTO_Link {
        public String groupId;  // Идентификатор группы
        public String noteId;   // Идентификатор заметки
        /*
         * Конструктор для создания пустого объекта для Firestore
         */
        public DTO_Link() {
        }
        /*
         * Конструктор для создания объекта с начальными значениями
         * @param groupId Идентификатор группы
         * @param noteId Идентификатор заметки
         */
        public DTO_Link(@NonNull GroupNoteLink groupNoteLink) {
            this.groupId = groupNoteLink.getGroupId();
            this.noteId = groupNoteLink.getNoteId();
        }
        // Метод для получения объекта GroupNoteLink из объекта DTO_GroupNodeLink
        public GroupNoteLink toGroup(@NonNull String id) {
            return new GroupNoteLink(id, groupId, noteId);
        }
    }
}
