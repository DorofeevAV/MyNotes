package com.dorofeev.mynotes;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/*
 * Тест который выполняется на устройстве Android
 * Проверяет работу с Firestore
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FirestoreTest {
    // Константы
    private static final int TIMEOUT_SECONDS = 30; // Время ожидания в секундах выполнения операции
    // Тесты для проверки работы с Firestore
    // Успешный результат - пользователи которые заданы в БД
    @Test
    public void test_01_LoadUsersFromFirestore() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CountDownLatch latch = new CountDownLatch(1);

        db.collection("users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        assertNotNull("Должны быть пользователи", queryDocumentSnapshots);
                        assertTrue("Должен быть хотя бы один пользователь",
                                !queryDocumentSnapshots.isEmpty());

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String username = doc.getString("username");
                            Log.d("FirestoreTest", "Найден пользователь: " + username);
                            assertNotNull("У пользователя должно быть имя", username);
                        }
                        latch.countDown();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        fail("Ошибка загрузки пользователей: " + e.getMessage());
                        latch.countDown();
                    }
                });

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

}