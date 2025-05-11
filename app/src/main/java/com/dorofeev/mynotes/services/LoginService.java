package com.dorofeev.mynotes.services;

import androidx.annotation.NonNull;

import com.dorofeev.mynotes.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/*
 * Синглтон - сервис для работы с пользователями
 * Загрузка пользователей из Firestore
 * Установка текущего пользователя
 */
public class LoginService {
    private static LoginService instance; // Экземпляр сервиса
    private User currentUser; // Текущий пользователь
    private final CollectionReference collectionRef; // Ссылка на коллекцию пользователей
    /*
     * Получение экземпляра сервиса
     * @return Экземпляр сервиса
     */
    public synchronized static LoginService getInstance() {
        if (instance == null) {
            instance = new LoginService();
        }
        return instance;
    }
    // Конструктор
    private LoginService() {
        collectionRef = FirebaseFirestore.getInstance().collection("users");
    }
    // Сallback интерфейс для обработки результата загрузки пользователей
    public interface UsersLoadedCallback {
        /* Метод для обработки успешной загрузки пользователей
         * @param users Список пользователей
         */
        void onUsersLoaded(List<User> users);
        /* Метод для обработки ошибки загрузки пользователей
         * @param e Исключение
         */
        void onError(Exception e);
    }
    // Callback интерфейс для обработки входа пользователя
    public interface UserLoginCallback {
        /* Метод для обработки успешного логина пользователя
         * @param user Пользователь
         */
        void onUserLoggedIn(User user);
        /* Метод для обработки ошибки логина пользователя
         * @param e Исключение
         */
        void onError(Exception e);
    }
    /*
     * Загрузка всех пользователей
     * @param callback Callback для обработки результата
     */
    public void getUsers(@NonNull final UsersLoadedCallback callback) {
        collectionRef
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<User> users = new ArrayList<User>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            DTO_User dto = doc.toObject(DTO_User.class);
                            if (dto.getUsername() != null) {
                                users.add(dto.toUser(doc.getId()));
                            }
                        }
                        callback.onUsersLoaded(users);
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
     * Логин пользователя по ID с проверкой в БД
     * @param userId ID пользователя
     * @param callback Callback для обработки результата
     */
    public void loginUser(@NonNull final String userId, @NonNull final UserLoginCallback callback) {
        collectionRef
                .document(userId)
                .get()
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<com.google.firebase.firestore.DocumentSnapshot>() {
                    @Override
                    public void onSuccess(com.google.firebase.firestore.DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            DTO_User dto = documentSnapshot.toObject(DTO_User.class);
                            if (dto != null && dto.getUsername() != null) {
                                currentUser = dto.toUser(userId);
                                callback.onUserLoggedIn(currentUser);
                            } else {
                                callback.onError(new Exception("User data is invalid or incomplete"));
                            }
                        } else {
                            callback.onError(new Exception("User not found"));
                        }
                    }
                })
                .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /*
     * Получить текущего пользователя после успешного входа
     * @return Текущий пользователь
     * @throws IllegalStateException - Если пользователь не вошёл в систему
     */
    public User getCurrentUser() throws IllegalStateException {
        if (currentUser == null) {
            throw new IllegalStateException("User not logged in");
        }
        return currentUser;
    }
    /*
     * DTO класс пользователя для Firebase
     */
    public static class DTO_User {
        private String username; // Имя пользователя
        // Пустой конструктор для Firebase
        public DTO_User() {
        }
        // getter и setter для имени пользователя
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
       /*
        * Метод для преобразования DTO в объект пользователя
        * @param id ID пользователя
        */
        public User toUser(@NonNull String id) {
            return new User(id, username);
        }
    }
}
