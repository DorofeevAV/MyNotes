package com.dorofeev.mynotes;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.services.GroupService;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GroupServiceTest {
    private static final int TIMEOUT_SECONDS = 30;
    /*
     * Метод для инициализации GroupService и ожидания загрузки групп
     * @return Инициализированный GroupService
     * @throws Exception
     */
    private GroupService init() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        GroupService groupService = new GroupService(new GroupService.GroupsChangedListener() {
            @Override
            public void onGroupsChanged(List<Group> groups) {
                System.out.println("Группы изменились: " + groups.size());
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                latch.countDown();
                throw new RuntimeException("Ошибка при получении групп", e);
            }
        });

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS); // Блокируем поток до загрузки

        return groupService;
    }

    @Test
    public void test_01_CreateGroup() throws Exception {
        GroupService groupService = init();

        CountDownLatch latch = new CountDownLatch(1);

        String groupName = "test_" + UUID.randomUUID();

        groupService.createGroup(groupName, new GroupService.OperationCallback() {
            @Override
            public void onSuccess(Group createdGroup) {
                System.out.println("Группа успешно создана: " + createdGroup.getName());
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Ошибка создания новой группы: " + e.getMessage());
            }
        });

        assertTrue("Создание новой группы не завершилось вовремя", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        boolean found = groupService.getGroups().stream().anyMatch(g -> groupName.equals(g.getName()));

        assertTrue("Созданная группа не найдена в списке групп", found);
    }

    @Test
    public void test_02_GetGroups() throws Exception {
        GroupService groupService = init();
        List<Group> groups = groupService.getGroups();

        assertNotNull("Полученный список групп не должен быть null", groups);

        System.out.println("Найдено групп: " + groups.size());
        for (Group group : groups) {
            System.out.println("Группа: " + group.getName() + " (ID: " + group.getId() + ")");
        }
    }

    @Test
    public void test_03_RenameAndVerifyGroups() throws Exception {
        GroupService groupService = init();
        List<Group> groups = groupService.getGroups();

        if (groups.isEmpty()) {
            fail("Нет групп для переименования");
        }

        System.out.println("Найдено групп для переименования: " + groups.size());

        Map<String, String> renamedGroups = new HashMap<>();
        int index = 1;

        for (Group group : groups) {
            CountDownLatch latchUpdate = new CountDownLatch(1);

            String newName = "Group_" + index++;
            renamedGroups.put(group.getId(), newName);
            group.setName(newName);

            groupService.updateGroup(group, new GroupService.OperationCallback() {
                @Override
                public void onSuccess(Group updatedGroup) {
                    System.out.println("Группа успешно переименована в: " + updatedGroup.getName());
                    latchUpdate.countDown();
                }

                @Override
                public void onError(Exception e) {
                    fail("Ошибка переименования группы: " + e.getMessage());
                }
            });

            assertTrue("Переименование группы не завершилось вовремя", latchUpdate.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        }

        List<Group> updatedGroups = groupService.getGroups();

        assertNotNull("Ошибка повторной загрузки групп", updatedGroups);

        for (Group updatedGroup : updatedGroups) {
            String expectedName = renamedGroups.get(updatedGroup.getId());
            if (expectedName != null) {
                assertEquals("Имя группы не соответствует ожидаемому", expectedName, updatedGroup.getName());
                System.out.println("Проверено: группа " + updatedGroup.getId() + " имеет имя " + expectedName);
            }
        }
    }

    @Test
    public void test_04_DeleteAllGroups() throws Exception {
        GroupService groupService = init();
        List<Group> groups = groupService.getGroups();

        if (groups.isEmpty()) {
            fail("Нет групп для удаления");
        }

        System.out.println("Найдено групп для удаления: " + groups.size());

        for (Group group : groups) {
            CountDownLatch latchDelete = new CountDownLatch(1);

            groupService.deleteGroup(group.getId(), new GroupService.DeleteCallback() {
                @Override
                public void onSuccess(String groupId) {
                    System.out.println("Удалена группа ID: " + groupId);
                    latchDelete.countDown();
                }

                @Override
                public void onError(Exception e) {
                    fail("Ошибка удаления группы: " + e.getMessage());
                }
            });

            assertTrue("Удаление группы не завершилось вовремя", latchDelete.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        }

        List<Group> updatedGroups = groupService.getGroups();
        assertNotNull("Ошибка повторной загрузки групп", updatedGroups);
        assertTrue("Группы остались после удаления", updatedGroups.isEmpty());
    }
}
