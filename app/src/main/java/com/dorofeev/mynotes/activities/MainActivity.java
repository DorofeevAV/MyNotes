package com.dorofeev.mynotes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import android.view.View;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dorofeev.mynotes.R;
import com.dorofeev.mynotes.adapters.GroupAdapter;
import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.models.Note;
import com.dorofeev.mynotes.services.GroupNoteManager;
import com.dorofeev.mynotes.services.GroupService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Главный экран приложения — список групп заметок
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewGroups;    // Список групп
    private GroupAdapter groupAdapter; // Адаптер для списка групп
    private GroupNoteManager manager; // Менеджер групп и заметок
    private EditText editTextSearch; // Поле для поиска заметок
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Инициализация элементов интерфейса
        recyclerViewGroups = findViewById(R.id.recyclerViewGroups);
        int spanCount = getResources().getConfiguration().orientation ==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;

        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        recyclerViewGroups.setLayoutManager(layoutManager);
        // Инициализация адаптера
        groupAdapter = new GroupAdapter();
        recyclerViewGroups.setAdapter(groupAdapter);
        // Инициализация менеджера групп и заметок
        manager = GroupNoteManager.getInstance();
        manager.setStructureChangedCallback(new GroupNoteManager.StructureChangedCallback() {
            @Override
            public void onStructureChanged(Map<Group, List<Note>> currentStructure) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        groupAdapter.updateData(currentStructure);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        // Подписка на изменения в группах
        findViewById(R.id.fabAddGroup).setOnClickListener(this::showFabActionsMenu);
        editTextSearch = findViewById(R.id.editTextSearch);
        findViewById(R.id.buttonSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = editTextSearch.getText().toString().trim().toLowerCase();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
            }
        });
    }
    // Показать меню действий для создания группы или заметки
    private void showFabActionsMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.inflate(R.menu.menu_fab_actions);
        // Скрыть пункт создания заметки, если групп нет
        if (groupAdapter.getItemCount() == 0) {
            popup.getMenu().findItem(R.id.menu_create_note).setVisible(false);
        }
        // Обработчик клика по пунктам меню
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(android.view.MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.menu_create_group) {
                    showCreateGroupDialog();
                    return true;
                } else if (id == R.id.menu_create_note) {
                    Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                    intent.putExtra("mode", "create");
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        // Показать меню
        popup.show();
    }
    /*
     * Показать диалог для создания новой группы
     */
    private void showCreateGroupDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_group, null);
        // Скрыть кнопки обновления и удаления
        dialogView.findViewById(R.id.buttonUpdate).setVisibility(View.GONE);
        dialogView.findViewById(R.id.buttonDelete).setVisibility(View.GONE);
        // Инициализация полей ввода
        EditText editTextGroupName = dialogView.findViewById(R.id.editTextGroupName);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        // Обработчик клика по кнопке создания группы
        dialogView.findViewById(R.id.buttonCreate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = editTextGroupName.getText().toString().trim();
                if (!groupName.isEmpty()) {
                    manager.createGroup(groupName, new GroupService.OperationCallback() {
                        @Override
                        public void onSuccess(Group group) {
                            Toast.makeText(MainActivity.this, "Группа создана", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(MainActivity.this, "Ошибка создания группы: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Введите название группы", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogView.findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // Показать диалог
        dialog.show();
    }
    /*
     * Выполнить поиск заметок по запросу
     * @param query Запрос для поиска
     */
    private void performSearch(String query) {
        // Списки
        List<Note> result = new ArrayList<Note>();
        List<Note> titles = new ArrayList<Note>();
        List<Note> tags = new ArrayList<Note>();
        List<Note> contents = new ArrayList<Note>();
        // Обход всех заметок
        Map<Group, List<Note>> all = manager.getStructureSnapshot();
        for (List<Note> notes : all.values()) {
            for (Note note : notes) {
                boolean matched = false;
                // Проверка заголовка
                if (note.getTitle() != null && note.getTitle().toLowerCase().contains(query)) {
                    titles.add(note);
                    matched = true;
                }
                // Проверка тегов
                if (!matched) {
                    for (String tag : note.getTags()) {
                        if (tag.toLowerCase().contains(query)) {
                            tags.add(note);
                            matched = true;
                            break;
                        }
                    }
                }
                // Проверка содержимого
                if (!matched && note.getContentMarkdown() != null &&
                        note.getContentMarkdown().toLowerCase().contains(query)) {
                    contents.add(note);
                }
            }
        }
        // Объединение результатов
        result.addAll(titles);
        result.addAll(tags);
        result.addAll(contents);
        // Переход к активности результатов поиска
        Intent intent = new Intent(MainActivity.this, SearchResultsActivity.class);
        intent.putExtra("query", query);
        intent.putExtra("notes", new ArrayList<>(result));
        startActivity(intent);
    }
}
