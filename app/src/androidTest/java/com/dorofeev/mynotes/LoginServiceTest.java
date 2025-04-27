package com.dorofeev.mynotes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.dorofeev.mynotes.models.User;
import com.dorofeev.mynotes.services.LoginService;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class LoginServiceTest {
    @Test
    public void testLoginProcess() throws InterruptedException {
        /* Тест для проверки процесса логина
         * 1. Получить список пользователей
         * 2. Попытаться залогиниться с неверным ID
         * 3. Проверить что currentUser выдает исключение
         * 4. Попытаться залогиниться с правильным ID
         * 5. Проверить что currentUser не выдает исключение и совпадает с правильным ID
         */
        LoginService loginService = new LoginService();
        CountDownLatch latch = new CountDownLatch(1);

        // Шаг 1: получить список пользователей
        loginService.getUsers(new LoginService.UsersLoadedCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                assertNotNull("Список пользователей не должен быть null", users);
                assertFalse("Список пользователей не должен быть пустым", users.isEmpty());

                User knownUser = users.get(0); // Берём первого пользователя для теста
                String knownUserId = knownUser.getId();

                // Шаг 2: попытка логина неверным ID
                loginService.loginUser("non_existing_user_id", new LoginService.UserLoginCallback() {
                    @Override
                    public void onUserLoggedIn(User user) {
                        fail("Не должно быть найдено пользователя с неверным ID");
                        latch.countDown();
                    }

                    @Override
                    public void onError(Exception e) {
                        assertNotNull("Должна быть ошибка при логине неверного ID", e);

                        // Пробуем неизвестный Id
                        try {
                            loginService.getCurrentUser();
                            fail("Должно было быть выброшено исключение");
                        } catch (IllegalStateException es) {
                        }
                        // Теперь пробуем логиниться правильным ID
                        loginService.loginUser(knownUserId, new LoginService.UserLoginCallback() {
                            @Override
                            public void onUserLoggedIn(User user) {

                                assertNotNull("Пользователь должен быть найден", user);

                                // Проверка текущего пользователя
                                User currentUser = loginService.getCurrentUser();
                                assertNotNull("Текущий пользователь должен быть установлен", currentUser);
                                assertEquals("ID текущего пользователя должен совпадать", knownUserId, currentUser.getId());

                                latch.countDown();
                            }

                            @Override
                            public void onError(Exception e) {
                                fail("Ошибка при логине правильного пользователя: " + e.getMessage());
                                latch.countDown();
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                fail("Ошибка загрузки пользователей: " + e.getMessage());
                latch.countDown();
            }
        });

        latch.await(30, TimeUnit.SECONDS);
    }
}
