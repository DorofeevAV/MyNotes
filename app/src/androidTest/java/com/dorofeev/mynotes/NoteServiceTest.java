package com.dorofeev.mynotes;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dorofeev.mynotes.models.Note;
import com.dorofeev.mynotes.services.NoteService;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Тесты для NoteService: создание, изменение, удаление заметки
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NoteServiceTest {
    private static final int TIMEOUT_SECONDS = 30;
    private Note createdNote;
    /*
     * Метод для инициализации NoteService и ожидания загрузки групп
     * @return Инициализированный NoteService
     * @throws Exception
     */
    private NoteService init() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        NoteService noteService = new NoteService(new NoteService.NotesChangedListener() {
            @Override
            public void onNotesChanged(List<Note> notes) {
                System.out.println("Группы изменились: " + notes.size());
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                latch.countDown();
                throw new RuntimeException("Ошибка при получении групп", e);
            }
        });
        latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS); // Блокируем поток до загрузки
        //
        return noteService;
    }

    @Test
    public void test_01_CreateNote() throws Exception {
        NoteService noteService = init();

        CountDownLatch latch = new CountDownLatch(1);
        noteService.createNote("Заголовок теста",
                     "Содержимое **markdown**",
                            "https://example.com/image.jpg",
                                    Arrays.asList("https://example.com/file.pdf"),
                                    Arrays.asList("тест", "заметка"),
                new NoteService.OperationCallback() {
            @Override
            public void onSuccess(Note note) {
                createdNote = note;
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Ошибка при создании заметки" + e.getMessage());
                latch.countDown();
            }
        });
        //
        assertTrue("Создание заметки не завершилось вовремя", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertNotNull("Созданная заметка не должна быть null", createdNote);
        assertNotNull("ID заметки должен быть установлен", createdNote.getId());
    }

    @Test
    public void test_02_UpdateNote() throws Exception {
        NoteService noteService = init();
        // Загружаем первую заметку из базы
        List<Note> notes = noteService.getNotes();
        assertFalse("Нет доступных заметок для обновления", notes.isEmpty());

        Note noteToUpdate = notes.get(0);
        noteToUpdate.setTitle("Изменённый заголовок");
        noteToUpdate.setContentMarkdown("**Новый текст**");

        CountDownLatch latch = new CountDownLatch(1);

        noteService.updateNote(noteToUpdate, new NoteService.OperationCallback() {
            @Override
            public void onSuccess(Note note) {
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Ошибка при обновлении заметки" + e.getMessage());
                latch.countDown();
            }
        });
        assertTrue("Обновление заметки не завершилось вовремя", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void test_03_LoadAllNotes() throws Exception {
        NoteService noteService = init();
        //
        List<Note> notes = noteService.getNotes();
        assertNotNull("Список заметок не должен быть null", notes);
        assertFalse("Список заметок должен содержать хотя бы одну", notes.isEmpty());

        for (Note n : notes) {
            System.out.println("Заметка: " + n.getTitle());
        }
    }

    @Test
    public void test_04_DeleteAllNotes() throws Exception {
        NoteService noteService = init();
        //
        CountDownLatch latch = new CountDownLatch(1);
        List<String> notes =  noteService.getNotes().stream()
                .map(Note::getId)
                .collect(Collectors.toList());
        //
        assertFalse("Нет заметок для удаления", notes.isEmpty());
        noteService.deleteNotes(notes, new NoteService.DeleteCallback() {
            @Override
            public void onSuccess(List<String> noteId) {
                System.out.println("Удалено " + noteId.size() + " узлов");
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail("Ошибка при удалении заметок" + e.getMessage());
                latch.countDown();
            }
        });

        assertTrue("Удаление заметки не завершилось вовремя", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

    }
}