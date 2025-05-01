package com.dorofeev.mynotes;

import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.models.Note;
import com.dorofeev.mynotes.services.GroupService;
import com.dorofeev.mynotes.services.NoteService;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class NoteServiceTest {

    private NoteService noteService;
    private GroupService groupService;
    private FirebaseFirestore db;

    @Before
    public void setUp() {
        noteService = new NoteService();
        groupService = new GroupService();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Тест: создание заметки после создания группы
     */
    @Test
    public void testCreateNoteWithGroup() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Group group = new Group(null, "Тестовая группа");

        groupService.createGroup(group, new GroupService.OperationCallback() {
            @Override
            public void onSuccess() {
                // Получаем ID новой группы через поиск по имени
                db.collection("groups")
                        .whereEqualTo("name", "Тестовая группа")
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (!snapshot.isEmpty()) {
                                String groupId = snapshot.getDocuments().get(0).getId();

                                Note note = new Note("Тестовая заметка",
                                        "Это содержимое в *Markdown* формате",
                                        groupId,
                                        null,
                                        List.of("https://storage.example.com/file1.pdf"),
                                        Arrays.asList("тест", "важно"));

                                noteService.createNote(note, new NoteService.OperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        assertTrue(true);
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        fail("Ошибка при создании заметки: " + e.getMessage());
                                        latch.countDown();
                                    }
                                });
                            } else {
                                fail("Не удалось получить ID созданной группы");
                                latch.countDown();
                            }
                        })
                        .addOnFailureListener(e -> {
                            fail("Ошибка поиска группы: " + e.getMessage());
                            latch.countDown();
                        });
            }

            @Override
            public void onError(Exception e) {
                fail("Ошибка при создании группы: " + e.getMessage());
                latch.countDown();
            }
        });

        assertTrue("Создание заметки с группой не завершилось вовремя", latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void testUpdateNoteWithoutId() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Note note = new Note(); // без ID

        noteService.updateNote(note, new NoteService.OperationCallback() {
            @Override
            public void onSuccess() {
                fail("Обновление не должно быть успешным без ID");
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                assertTrue(e instanceof IllegalArgumentException);
                latch.countDown();
            }
        });

        assertTrue("Проверка ошибки при обновлении без ID", latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void testDeleteFakeNote() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        noteService.deleteNote("non_existing_id", new NoteService.OperationCallback() {
            @Override
            public void onSuccess() {
                assertTrue(true);
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Ошибка при удалении несуществующей заметки: " + e.getMessage());
                latch.countDown();
            }
        });

        assertTrue("Удаление должно завершиться", latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testSearchNotes() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        noteService.searchNotes("тест", new NoteService.NotesChangedListener() {
            @Override
            public void onNotesChanged(List<Note> notes) {
                assertNotNull(notes);
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Ошибка поиска: " + e.getMessage());
                latch.countDown();
            }
        });

        assertTrue("Поиск заметок должен завершиться", latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testStopListeningNotesDoesNotThrow() {
        try {
            noteService.stopListeningNotes();
        } catch (Exception e) {
            fail("Метод stopListeningNotes() вызвал исключение: " + e.getMessage());
        }
    }
}
