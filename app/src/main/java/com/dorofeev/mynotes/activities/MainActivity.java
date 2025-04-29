package com.dorofeev.mynotes.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import android.view.View;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dorofeev.mynotes.R;
import com.dorofeev.mynotes.adapters.GroupAdapter;
import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.services.GroupService;
import com.dorofeev.mynotes.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * Главный экран приложения — список групп заметок
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewGroups;
    private GroupAdapter groupAdapter;
    private GroupService groupService;
    private List<Group> groups = new ArrayList<>();
    private EditText editTextSearch;
    private Button buttonSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Настройка отступов для системных панелей
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerViewGroups = findViewById(R.id.recyclerViewGroups);
        editTextSearch = findViewById(R.id.editTextSearch);
        buttonSearch = findViewById(R.id.buttonSearch);

        recyclerViewGroups.requestFocus();

        // Настройка сетки групп
        int spanCount = getResources().getConfiguration().orientation ==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;

        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        recyclerViewGroups.setLayoutManager(layoutManager);

        // Инициализация адаптера
        groupAdapter = new GroupAdapter(groups);
        recyclerViewGroups.setAdapter(groupAdapter);

        // Инициализация сервиса групп
        groupService = new GroupService();

        // Подписка на изменения групп
        startListeningGroups();

        // Обработка поиска
        buttonSearch.setOnClickListener(v -> {
            String query = editTextSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                filterGroups(query);
            } else {
                Toast.makeText(this, "Введите текст для поиска", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.fabAddGroup).setOnClickListener(v -> showFabActionsMenu(v));
    }

    /**
     * Начинаем слушать изменения в списке групп
     */
    private void startListeningGroups() {
        groupService.startListeningGroupChange(new GroupService.GroupsChangedListener() {
            @Override
            public void onGroupsChanged(List<Group> updatedGroups) {
                groups.clear();
                groups.addAll(updatedGroups);
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, "Ошибка загрузки групп", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Фильтрация списка групп по тексту поиска
     */
    private void filterGroups(String query) {
        List<Group> filtered = new ArrayList<>();
        for (Group group : groups) {
            if (group.getName() != null && group.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(group);
            }
        }
        groupAdapter.updateData(filtered);
    }
    private void showFabActionsMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.inflate(R.menu.menu_fab_actions);

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_create_group) {
                showCreateGroupDialog();
                return true;
            } else if (id == R.id.menu_create_note) {
                Toast.makeText(this, "Создание заметки пока не реализовано", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popup.show();
    }
    private void showCreateGroupDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_group, null);
        EditText editTextGroupName = dialogView.findViewById(R.id.editTextGroupName);
        Button buttonCreate = dialogView.findViewById(R.id.buttonCreate);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        buttonCreate.setOnClickListener(v -> {
            String groupName = editTextGroupName.getText().toString().trim();
            if (!groupName.isEmpty()) {
                Group newGroup = new Group(null, groupName);
                ServiceLocator.getGroupService().createGroup(newGroup, new GroupService.OperationCallback() {
                    @Override
                    public void onSuccess() {
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
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
