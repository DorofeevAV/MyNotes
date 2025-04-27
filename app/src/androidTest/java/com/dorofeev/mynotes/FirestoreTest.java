package com.dorofeev.mynotes;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class FirestoreTest {
    @Test
    public void testLoadUsersFromFirestore() throws InterruptedException {
        // Тест для проверки загрузки пользователей из Firestore
        // Успешный результат - пользователи которые заданы в БД
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CountDownLatch latch = new CountDownLatch(1);

        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    assertNotNull("Должны быть пользователи", queryDocumentSnapshots);
                    assertTrue("Должен быть хотя бы один пользователь", !queryDocumentSnapshots.isEmpty());

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String username = doc.getString("username");
                        Log.d("FirestoreTest", "Найден пользователь: " + username);
                        assertNotNull("У пользователя должно быть имя", username);
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail("Ошибка загрузки пользователей: " + e.getMessage());
                    latch.countDown();
                });

        // Ждём максимум 10 секунд выполнения
        latch.await(20, TimeUnit.SECONDS);
    }
}