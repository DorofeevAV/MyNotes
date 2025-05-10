package com.dorofeev.mynotes;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dorofeev.mynotes.models.GroupNoteLink;
import com.dorofeev.mynotes.services.GroupNoteLinkService;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/*
 * Тесты для GroupNoteLinkService:
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GroupNoteLinkServiceTest {

    private static final int TIMEOUT_SECONDS = 30;

    /*
     * Метод для инициализации GroupService и ожидания загрузки групп
     * @return Инициализированный GroupService
     * @throws Exception
     */
    private GroupNoteLinkService init() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        GroupNoteLinkService groupService = new GroupNoteLinkService(new GroupNoteLinkService.LinksChangedListener() {
            @Override
            public void onLinksChanged(List<GroupNoteLink> links) {
                System.out.println("Группы изменились: " + links.size());
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                throw new RuntimeException("Ошибка при получении групп", e);
            }
        });

        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS); // Блокируем поток до загрузки

        return groupService;
    }
    // Тест создания связи между группой и заметкой
    @Test
    public void test01_createLink() throws Exception {
        GroupNoteLinkService groupNoteLinkService = init();
        //
        CountDownLatch latch = new CountDownLatch(1);
        String groupId = "group_" + UUID.randomUUID();
        String noteId = "note_" + UUID.randomUUID();

        groupNoteLinkService.createLink(groupId, noteId, new GroupNoteLinkService.OperationCallback() {
            @Override
            public void onSuccess(GroupNoteLink link) {
                System.out.println("Связь успешно создана");
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Ошибка создания связи: " + e.getMessage());
            }
        });

        assertTrue("Создание связи не завершилось", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }
    // Тест загрузки всех связей
    @Test
    public void test02_loadLinks() throws Exception {
        GroupNoteLinkService groupNoteLinkService = init();
        //
        List<GroupNoteLink> links = groupNoteLinkService.getLinks();
        assertNotNull("Полученный список групп не должен быть null", links);

        System.out.println("Найдено связей: " + links.size());
        for (GroupNoteLink link : links) {
            System.out.println("ID Группа: " + link.getGroupId() +
                    ", ID Заметка: " + link.getNoteId() +
                    " (ID: " + link.getId() + ")");
        }
    }
    // Тест обновления связи
    @Test
    public void test03_updateLink() throws Exception {
        GroupNoteLinkService groupNoteLinkService = init();
        //
        List<GroupNoteLink> current = groupNoteLinkService.getLinks();
        if (current.isEmpty()) fail("Нет данных для обновления");

        GroupNoteLink link = current.get(0);
        String newNoteId = "note_updated_" + UUID.randomUUID();
        link.setNoteId(newNoteId);

        CountDownLatch latch = new CountDownLatch(1);

        groupNoteLinkService.updateLink(link, new GroupNoteLinkService.OperationCallback() {
            @Override
            public void onSuccess(GroupNoteLink link) {
                System.out.println("Обновление выполнено");
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Ошибка обновления: " + e.getMessage());
            }
        });

        assertTrue("Обновление не завершено", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }
    // Тест удаления всех связей
    @Test
    public void test04_removeAllLinks() throws Exception {
        GroupNoteLinkService groupNoteLinkService = init();
        //
        List<String> links = groupNoteLinkService.getLinks().stream()
                .map(GroupNoteLink::getId)
                .collect(Collectors.toList());
        //
        CountDownLatch latch = new CountDownLatch(1);
        groupNoteLinkService.deleteLinks(links, new GroupNoteLinkService.DeleteCallback() {
            @Override
            public void onSuccess(List<String> linkIds) {
                System.out.println("Удалено "+ linkIds.size() + " связей");
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Ошибка удаления: " + e.getMessage());
            }
        });

        assertTrue("Удаление не завершено", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }
}
