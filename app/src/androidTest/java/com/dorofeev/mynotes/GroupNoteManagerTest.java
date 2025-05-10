package com.dorofeev.mynotes;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.models.Note;
import com.dorofeev.mynotes.services.GroupNoteManager;
import com.dorofeev.mynotes.services.GroupService;
import com.dorofeev.mynotes.services.NoteService;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/*
 * Тесты для GroupNoteManager
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GroupNoteManagerTest {
    private static final int TIMEOUT_SECONDS = 30;
    private GroupNoteManager init() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        GroupNoteManager.getInstance().setStructureChangedCallback(new GroupNoteManager.StructureChangedCallback() {
            @Override
            public void onStructureChanged(Map<Group, List<Note>> currentStructure) {
                latch.countDown();
            }
            @Override
            public void onError(Exception e) {
                latch.countDown();
                throw new RuntimeException("Ошибка при получении структуры", e);
            }
        });
        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS); // Блокируем поток до загрузки
        return GroupNoteManager.getInstance();
    }
    // Тест создания двух групп с заметками
    @Test
    public void test_01_CreateTwoGroupsWithNotes() throws Exception {
        GroupNoteManager manager = init();

        CountDownLatch createLatch = new CountDownLatch(6);

        manager.createGroup("Группа 1", new GroupService.OperationCallback() {
            @Override
            public void onSuccess(Group group1) {
                for (int i = 1; i <= 3; i++) {
                    manager.createNote(group1, "Note A" + i, "Content A" + i, null, Collections.<String>emptyList(), Collections.<String>emptyList(), new NoteService.OperationCallback() {
                        @Override
                        public void onSuccess(Note note) { createLatch.countDown(); }
                        @Override
                        public void onError(Exception e) { fail(e.getMessage()); }
                    });
                }
                manager.createGroup("Группа 2", new GroupService.OperationCallback() {
                    @Override
                    public void onSuccess(Group group2) {
                        for (int i = 1; i <= 3; i++) {
                            manager.createNote(group2, "Note B" + i, "Content B" + i, null, Collections.<String>emptyList(), Collections.<String>emptyList(), new NoteService.OperationCallback() {
                                @Override
                                public void onSuccess(Note note) { createLatch.countDown(); }
                                @Override
                                public void onError(Exception e) { fail(e.getMessage()); }
                            });
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        fail(e.getMessage());
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                fail(e.getMessage());
            }
        });

        assertTrue("Создание заметок не завершилось вовремя", createLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }
    // Тест чтения структуры групп и заметок
    @Test
    public void test_02_ReadStructure() throws Exception {
        GroupNoteManager manager = init();

        Map<Group, List<Note>> structure = manager.getStructureSnapshot();
        assertNotNull("Структура не должна быть null", structure);
        assertFalse("Структура должна содержать хотя бы одну группу", structure.isEmpty());

        System.out.println("==== Дерево заметок ====");
        for (Map.Entry<Group, List<Note>> entry : structure.entrySet()) {
            System.out.println("Группа: " + entry.getKey().getName());
            for (Note note : entry.getValue()) {
                System.out.println("  - Заметка: " + note.getTitle());
            }
        }
    }
    // Тест переименования заметки
    @Test
    public void test_03_RenameNote() throws Exception {
        GroupNoteManager manager = init();

        Map<Group, List<Note>> structure = manager.getStructureSnapshot();
        Group group = structure.keySet().iterator().next();
        Note note = structure.get(group).get(0);

        note.setTitle("Обновлённый заголовок");
        CountDownLatch updateLatch = new CountDownLatch(1);
        manager.updateNote(note, null,  new NoteService.OperationCallback() {
            @Override
            public void onSuccess(Note n) {
                updateLatch.countDown();
            }
            @Override
            public void onError(Exception e) {
                fail(e.getMessage());
                updateLatch.countDown();
            }
        });
        assertTrue("Обновление заметки не завершилось вовремя", updateLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        structure = manager.getStructureSnapshot();
        // Найти обновлённую заметку
        for (Map.Entry<Group, List<Note>> entry : structure.entrySet()) {
            for (Note n : entry.getValue()) {
                if (n.getId().equals(note.getId())) {
                    note = n;
                    break;
                }
            }
        }
        assertTrue("Заголовок заметки не обновился", note.getTitle().equals("Обновлённый заголовок"));
    }
    // Тест перемещения заметки в другую группу
    @Test
    public void test_04_MoveNoteToAnotherGroup() throws Exception {
        GroupNoteManager manager = init();

        Map<Group, List<Note>> structure = manager.getStructureSnapshot();
        Iterator<Group> iterator = structure.keySet().iterator();
        Group group1 = iterator.next();
        Group group2 = iterator.next();
        Note note = structure.get(group1).get(2);

        CountDownLatch moveLatch = new CountDownLatch(1);
        manager.updateNote(note, group2, new NoteService.OperationCallback() {
            @Override
            public void onSuccess(Note n) {
                moveLatch.countDown();
            }
            @Override
            public void onError(Exception e) {
                fail(e.getMessage());
                moveLatch.countDown();
            }
        });
        assertTrue("Перемещение заметки не завершилось вовремя", moveLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }
    // Тест удаления заметки из группы
    @Test
    public void test_05_DeleteNoteFromGroup() throws Exception {
        GroupNoteManager manager = init();

        Map<Group, List<Note>> structure = manager.getStructureSnapshot();
        Group group = structure.keySet().stream().skip(1).findFirst().get();
        Note note = structure.get(group).get(0);

        CountDownLatch deleteLatch = new CountDownLatch(1);
        manager.deleteNote(note.getId(), new NoteService.DeleteCallback() {
            @Override
            public void onSuccess(List<String> noteIds) {
                deleteLatch.countDown();
            }
            @Override
            public void onError(Exception e) {
                fail(e.getMessage());
                deleteLatch.countDown();
            }
        });
        assertTrue("Удаление заметки не завершилось вовремя", deleteLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }
    // Тест удаления всех групп
    @Test
    public void test_06_DeleteAllGroups() throws Exception {
        GroupNoteManager manager = init();

        Map<Group, List<Note>> structure = manager.getStructureSnapshot();
        List<Group> groupsToDelete = new ArrayList<>(structure.keySet());

        for (Group group : groupsToDelete) {
            CountDownLatch deleteLatch = new CountDownLatch(1);
            manager.deleteGroup(group.getId(), new GroupService.DeleteCallback() {
                @Override
                public void onSuccess(String groupId) {
                    System.out.println("Удалена группа" + groupId);
                    deleteLatch.countDown();
                }
                @Override
                public void onError(Exception e) {
                    fail("Ошибка при удалении группы: " + e.getMessage());
                    deleteLatch.countDown();
                }
            });
            assertTrue("Удаление группы не завершилось вовремя", deleteLatch.await(TIMEOUT_SECONDS*10, TimeUnit.SECONDS));
        }
    }
}
