package com.dorofeev.mynotes.services;

import androidx.annotation.NonNull;

import com.dorofeev.mynotes.models.Group;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Cервис для работы с группами через Firestore
 */
public class GroupService {
    private final CollectionReference collectionRef; // Ссылка на коллекцию групп
    private final ListenerRegistration groupsListener;      // Слушатель изменений в коллекции
    private volatile List<Group> cachedGroups = new ArrayList<>();  // Кжшешированный список групп

    /*
     * Конструктор для создания объекта с начальными значениями
     */
    public GroupService( GroupsChangedListener listener ) {
        this.collectionRef = FirebaseFirestore.getInstance().collection("groups");
        //
        groupsListener = collectionRef.addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) {
                if (listener != null) listener.onError(e);
                return;
            }

            List<Group> newList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshots) {
                DTO_Group data = doc.toObject(DTO_Group.class);
                newList.add(data.toGroup((doc.getId())));
            }
            // Кэшируем новый список групп
            cachedGroups = Collections.unmodifiableList(newList);
            // Вызов события
            if (listener != null) {
                listener.onGroupsChanged(cachedGroups);
            }
        });
    }

    // callback интерфейс обратной связи для уведомления об изменении списка групп
    public interface GroupsChangedListener {
        void onGroupsChanged(List<Group> groups);   // Уведомление об обновлении
        void onError(Exception e); // Уведомление об ошибке
    }

    // callback интерфейс для уведомления об успехе или ошибке операций с группой
    public interface OperationCallback {
        void onSuccess(Group group); // Уведомление об успешной операции
        void onError(Exception e); // Уведомление об ошибке
    }

    // callback интерфейс для уведомления об успешном удалении группы
    public interface DeleteCallback {
        void onSuccess(String groupId); // Уведомление об успешном удалении
        void onError(Exception e); // Уведомление об ошибке
    }
    /*
     * Получить список всех групп, закешированный после последнего обновления из Firestore.
     * @return Невозможноизменяемый список групп
     */
    public List<Group> getGroups() {
        return cachedGroups;
    }

    /*
     * Создать новую группу в Firestore
     * @param dto DTO объекта группы с названием
     * @param callback Обработчик завершения операции
     */
    public void createGroup(@NonNull String name, @NonNull final OperationCallback callback) {
        String id = collectionRef.document().getId();
        Group group = new Group(id, name);
        // Создание новой группы в Firestore
        collectionRef
                .document(id)
                .set(new DTO_Group(group))
                .addOnSuccessListener(unused -> callback.onSuccess(group))
                .addOnFailureListener(callback::onError);
    }

    /*
     * Обновить существующую группу по её ID
     * @param group Группа с новыми данными
     * @param callback Обработчик завершения операции
     */
    public void updateGroup(@NonNull Group group, @NonNull final OperationCallback callback) {
        collectionRef
                .document(group.getId())
                .set(new DTO_Group(group), SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess(group))
                .addOnFailureListener(callback::onError);
    }

    /**
     * Удалить группу из Firestore по её ID
     * @param groupId Идентификатор группы, которую нужно удалить
     * @param callback Обработчик завершения операции
     */
    public void deleteGroup(@NonNull String groupId, @NonNull final DeleteCallback callback) {
        collectionRef
                .document(groupId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(groupId))
                .addOnFailureListener(callback::onError);
    }
    /*
     * Вложенный класс для представления группы в Firestore
     */
    private static class DTO_Group {
        private String name; // Имя группы
        /*
         * Конструктор для создания пустого объекта для firestore
         */
        public DTO_Group( ){
        }
        /*
         * Конструктор для создания объекта с начальными значениями
         * @param id ID группы в Firestore
         * @param group Объект данных из Firestore
         */
        public DTO_Group( @NonNull Group group) {
            name = group.getName();
        }
        // Геттер и сеттер для name геобходдимы для FireBase
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        // Метод для получения объекта Group из объекта DTO_Group
        public Group toGroup(@NonNull String id) {
            return new Group(id, name);
        }
    }
}
