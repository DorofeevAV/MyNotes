package com.dorofeev.mynotes;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dorofeev.mynotes.models.User;
import com.dorofeev.mynotes.services.LoginService;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/*
 * Тесты для LoginService
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoginServiceTest {
    private LoginService loginService;  // Сервис для работы с пользователями
    private static final int TIMEOUT_SECONDS = 30; // Время ожидания в секундах
    // Начальная настройка тестов - получение singleton экземпляра LoginService
    @Before
    public void setUp() {
        loginService = LoginService.getInstance();
    }
    /*
     * Вспомогательны метод для блокирующей загрузки пользователей
     * @return Список пользователей
     * @throws InterruptedException
     */
    private List<User> loadUsersBlocking() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<User>> resultHolder = new AtomicReference<>();

        loginService.getUsers(new LoginService.UsersLoadedCallback() {
            @Override
            public void onUsersLoaded(List<User> loadedUsers) {
                resultHolder.set(loadedUsers);
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Ошибка загрузки пользователей: " + e.getMessage());
                latch.countDown();
            }
        });

        assertTrue("Загрузка пользователей не завершилась вовремя",
                latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        List<User> users = resultHolder.get();
        assertNotNull("Список пользователей не должен быть null", users);
        assertFalse("Список пользователей не должен быть пустым", users.isEmpty());
        return users;
    }
    // Тест с входом неизвестного пользователя
    @Test
    public void test_01_LoginWithInvalidUserId() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        loginService.loginUser("non_existing_user_id", new LoginService.UserLoginCallback() {
            @Override
            public void onUserLoggedIn(User user) {
                fail("Не должно быть найдено пользователя с неверным ID");
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                assertNotNull("Должна быть ошибка при логине неверного ID", e);

                try {
                    loginService.getCurrentUser();
                    fail("Должно быть выброшено исключение при вызове getCurrentUser без логина");
                } catch (IllegalStateException ignored) {
                }

                latch.countDown();
            }
        });

        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue("Тест логина с неверным ID не завершился вовремя", completed);
    }
    // Тест с входом известного пользователя
    @Test
    public void test_02_LoginWithValidUserId() throws InterruptedException {
        List<User> users = loadUsersBlocking();
        User knownUser = users.get(0);
        String knownUserId = knownUser.getId();

        CountDownLatch latch = new CountDownLatch(1);

        loginService.loginUser(knownUserId, new LoginService.UserLoginCallback() {
            @Override
            public void onUserLoggedIn(User user) {
                assertNotNull("Пользователь должен быть найден", user);

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
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue("Тест логина с правильным ID не завершился вовремя", completed);
    }
}
