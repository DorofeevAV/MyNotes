package com.dorofeev.mynotes.services;

import androidx.annotation.NonNull;

import com.dorofeev.mynotes.models.Group;
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
 * Сервис для работы с группами заметок через Firestore с подпиской на изменения
 */
public class GroupService {

    private final FirebaseFirestore db;
    private ListenerRegistration groupsListener;

    public GroupService() {
        db = FirebaseFirestore.getInstance();
    }

    // Callback для подписки на изменения списка групп
    public interface GroupsChangedListener {
        void onGroupsChanged(List<Group> groups);
        void onError(Exception e);
    }

    // Callback для операций создания, обновления и удаления
    public interface OperationCallback {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * Начать слушать изменения списка групп
     * @param listener Обработчик изменений
     */
    public void startListeningGroupChange(@NonNull final GroupsChangedListener listener) {
        groupsListener = db.collection("groups")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            listener.onError(e);
                            return;
                        }
                        if (snapshots != null) {
                            List<Group> groups = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : snapshots) {
                                String id = doc.getId();
                                String name = doc.getString("name");
                                if (name != null) {
                                    groups.add(new Group(id, name));
                                }
                            }
                            listener.onGroupsChanged(groups);
                        }
                    }
                });
    }

    /**
     * Остановить прослушивание изменений списка групп
     */
    public void stopListeningGroupChange() {
        if (groupsListener != null) {
            groupsListener.remove();
            groupsListener = null;
        }
    }

    /**
     * Создать новую группу
     */
    public void createGroup(@NonNull Group group, @NonNull final OperationCallback callback) {
        db.collection("groups")
                .add(group)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Обновить существующую группу
     */
    public void updateGroup(@NonNull Group group, @NonNull final OperationCallback callback) {
        db.collection("groups")
                .document(group.getId())
                .update("name", group.getName())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Удалить группу
     */
    public void deleteGroup(@NonNull String groupId, @NonNull final OperationCallback callback) {
        db.collection("groups")
                .document(groupId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}
