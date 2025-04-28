package com.dorofeev.mynotes;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.services.GroupService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class GroupServiceTest {
    private GroupService groupService;
    private List<Group> loadedGroups;

    @Before
    public void setUp() {
        groupService = new GroupService();
    }
    /**
     * Метод для загрузки групп с ожиданием результата
     * @return Список загруженных групп
     * @throws InterruptedException Если операция прервана
     */
    private List<Group> loadGroups() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        groupService.startListeningGroupChange(new GroupService.GroupsChangedListener() {
            @Override
            public void onGroupsChanged(List<Group> groups) {
                loadedGroups = groups;
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Ошибка загрузки списка групп: " + e.getMessage());
            }
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Загрузка списка групп не завершилась вовремя", completed);

        assertNotNull("Список групп не должен быть null", loadedGroups);

        return loadedGroups;
    }
    /**
     * Тест: получить список всех групп
     */
    @Test
    public void testGetGroups() throws InterruptedException {
        List<Group> groups = loadGroups();

        assertNotNull("Полученный список групп не должен быть null", groups);

        System.out.println("Найдено групп: " + groups.size());
        for (Group group : groups) {
            System.out.println("Группа: " + group.getName() + " (ID: " + group.getId() + ")");
        }
    }
    /**
     * Тест: создание новой группы
     */
    @Test
    public void testCreateGroup() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        // Генерируем уникальное имя группы
        String groupName = "test_" + UUID.randomUUID().toString();
        Group newGroup = new Group(null, groupName);

        // Пытаемся создать новую группу
        groupService.createGroup(newGroup, new GroupService.OperationCallback() {
            @Override
            public void onSuccess() {
                System.out.println("Группа успешно создана: " + groupName);
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Ошибка создания новой группы: " + e.getMessage());
            }
        });
        // Ждем завершения операции максимум 10 секунд
        assertTrue("Создание новой группы не завершилось вовремя", latch.await(10, TimeUnit.SECONDS));

        // Дополнительно проверяем, что группа действительно появилась в базе
        List<Group> groups = loadGroups();

        boolean found = false;
        for (Group group : groups) {
            if (groupName.equals(group.getName())) {
                found = true;
                break;
            }
        }

        assertTrue("Созданная группа не найдена в списке групп", found);
    }
    /**
     * Тест: переименовать все группы и проверить переименование
     */
    @Test
    public void testRenameAndVerifyGroups() throws InterruptedException {
        // Шаг 1: загрузить все группы
        List<Group> groups = loadGroups();

        if (groups.isEmpty()) {
            System.out.println("Нет групп для переименования");
            return;
        }

        System.out.println("Найдено групп для переименования: " + groups.size());

        // Список для хранения ID -> новое имя
        Map<String, String> renamedGroups = new HashMap<>();

        int index = 1; // Нумерация для новых имен

        // Шаг 2: переименовать все группы
        for (Group group : groups) {
            CountDownLatch latchUpdate = new CountDownLatch(1);

            String newName = "Group_" + index++;
            renamedGroups.put(group.getId(), newName); // Запоминаем новое имя

            group.setName(newName);

            groupService.updateGroup(group, new GroupService.OperationCallback() {
                @Override
                public void onSuccess() {
                    System.out.println("Группа успешно переименована в: " + newName);
                    latchUpdate.countDown();
                }

                @Override
                public void onError(Exception e) {
                    fail("Ошибка переименования группы: " + e.getMessage());
                }
            });

            // Ждём завершения переименования одной группы
            assertTrue("Переименование группы не завершилось вовремя", latchUpdate.await(10, TimeUnit.SECONDS));
        }

        // Шаг 3: загрузить группы заново
        List<Group> updatedGroups = loadGroups();

        assertNotNull("Ошибка повторной загрузки групп", updatedGroups);

        // Шаг 4: проверить, что имена обновлены правильно
        for (Group updatedGroup : updatedGroups) {
            String expectedName = renamedGroups.get(updatedGroup.getId());
            if (expectedName != null) { // Если группа была переименована в этом тесте
                assertEquals("Имя группы не соответствует ожидаемому", expectedName, updatedGroup.getName());
                System.out.println("Проверено: группа " + updatedGroup.getId() + " имеет имя " + expectedName);
            }
        }
    }
    /**
     * Тест: удалить все группы
     */
    @Test
    public void testDeleteAllGroups() throws InterruptedException {
        // Шаг 1: загрузить все группы
        List<Group> groups = loadGroups();

        if (groups.isEmpty()) {
            System.out.println("Нет групп для удаления");
            return; // Нечего удалять — тест успешно пройден
        }

        System.out.println("Найдено групп для удаления: " + groups.size());

        // Шаг 2: удалить каждую группу
        for (Group group : groups) {
            CountDownLatch latchDelete = new CountDownLatch(1);

            groupService.deleteGroup(group.getId(), new GroupService.OperationCallback() {
                @Override
                public void onSuccess() {
                    System.out.println("Удалена группа: " + group.getName() + " (ID: " + group.getId() + ")");
                    latchDelete.countDown();
                }

                @Override
                public void onError(Exception e) {
                    fail("Ошибка удаления группы: " + e.getMessage());
                }
            });

            // Ждём завершения удаления одной группы
            assertTrue("Удаление группы не завершилось вовремя", latchDelete.await(10, TimeUnit.SECONDS));
        }

        // Шаг 3: загрузить группы снова и убедиться, что они удалены
        List<Group> updatedGroups = loadGroups();

        assertNotNull("Ошибка повторной загрузки групп", updatedGroups);
        assertTrue("Группы остались после удаления", updatedGroups.isEmpty());

        System.out.println("Все группы успешно удалены.");
    }

}
