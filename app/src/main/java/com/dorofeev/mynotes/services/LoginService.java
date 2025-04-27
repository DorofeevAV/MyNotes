package com.dorofeev.mynotes.services;

import androidx.annotation.NonNull;

import com.dorofeev.mynotes.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с пользователями
 * Загрузка пользователей из Firestore
 * Установка текущего пользователя
 */
public class LoginService {
    // Поля
    private User currentUser; // Текущий пользователь
    private final FirebaseFirestore db; // База данных Firestore
    // Конструктор
    public LoginService() {
        db = FirebaseFirestore.getInstance();
    }
    // Интерфейс для callback загрузки пользователей
    public interface UsersLoadedCallback {
        /** Метод для обработки успешной загрузки пользователей
         * @param users Список пользователей
         */
        void onUsersLoaded(List<User> users);
        /** Метод для обработки ошибки загрузки пользователей
         * @param e Исключение
         */
        void onError(Exception e);
    }
    // Интерфейс для callback логина пользователя
    public interface UserLoginCallback {
        /** Метод для обработки успешного логина пользователя
         * @param user Пользователь
         */
        void onUserLoggedIn(User user);
        /** Метод для обработки ошибки логина пользователя
         * @param e Исключение
         */
        void onError(Exception e);
    }

    /**
     * Загрузка всех пользователей
     * @param callback Callback для обработки результата
     */
    public void getUsers(@NonNull final UsersLoadedCallback callback) {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String username = doc.getString("username");
                        if (username != null) {
                            users.add(new User(id, username));
                        }
                    }
                    callback.onUsersLoaded(users);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Логин пользователя по ID с проверкой в БД
     * @param userId ID пользователя
     * @param callback Callback для обработки результата
     */
    public void loginUser(@NonNull String userId, @NonNull final UserLoginCallback callback) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        if (username != null) {
                            currentUser = new User(documentSnapshot.getId(), username);
                            callback.onUserLoggedIn(currentUser);
                        } else {
                            callback.onError(new Exception("User not found"));
                        }
                    } else {
                        callback.onError(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Получить текущего пользователя после успешного входа
     */
    public User getCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("User not logged in");
        }
        return currentUser;
    }
}
