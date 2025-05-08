package com.dorofeev.mynotes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
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
import com.dorofeev.mynotes.services.GroupNoteLinkService;
import com.dorofeev.mynotes.services.GroupService;
import com.dorofeev.mynotes.services.NoteService;

import java.util.ArrayList;
import java.util.List;

/*
 * Главный экран приложения — список групп заметок
 */
public class MainActivity extends AppCompatActivity {

   /* private RecyclerView recyclerViewGroups;
    private GroupAdapter groupAdapter;
    private GroupService groupService;
    private NoteService noteService;
    private GroupNoteLinkService groupNoteLinkService;
    private List<Note> allNotes = new ArrayList<>();
    private List<GroupNoteLink> allLinks = new ArrayList<>();
    private EditText editTextSearch;
    private Button buttonSearch;
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
  /*      //
        recyclerViewGroups = findViewById(R.id.recyclerViewGroups);
        editTextSearch = findViewById(R.id.editTextSearch);
        buttonSearch = findViewById(R.id.buttonSearch);

        // Настройка сетки групп
        int spanCount = getResources().getConfiguration().orientation ==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;

        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        recyclerViewGroups.setLayoutManager(layoutManager);

        // Получение всех сервисов
        groupService = ServiceLocator.getGroupService();
        noteService = ServiceLocator.getNoteService();
        groupNoteLinkService = ServiceLocator.getGroupNoteLinkService();

        // подписка на группы
        groupService.startListenGroups(new GroupService.GroupsChangedListener() {
            @Override
            public void onGroupsChanged(List<Group> updatedGroups) {
                updateGroupAdapter();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, "Ошибка загрузки групп", Toast.LENGTH_SHORT).show();
            }
        });

        // подписка на заметки
        noteService.startListenNotes(new NoteService.NotesChangedListener() {
            @Override
            public void onNotesChanged(List<Note> updatedNotes) {
                allNotes = updatedNotes;
                updateGroupAdapter();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, "Ошибка загрузки заметок", Toast.LENGTH_SHORT).show();
            }
        });

        // подписка на связи
        groupNoteLinkService.startListenLinks(new GroupNoteLinkService.OnLinksChangedListener() {
            @Override
            public void onLinksChanged(List<GroupNoteLink> updatedLinks) {
                allLinks = updatedLinks;
                updateGroupAdapter();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, "Ошибка загрузки связей", Toast.LENGTH_SHORT).show();
            }
        });
        // Инициализация адаптера
        groupAdapter = new GroupAdapter();
        recyclerViewGroups.setAdapter(groupAdapter);

        // Обработка нажатия плавающй кнопи добавить группу\заметку
        findViewById(R.id.fabAddGroup).setOnClickListener(v -> showFabActionsMenu(v));
   */ }
    private void updateGroupAdapter() {
     //   runOnUiThread(() -> groupAdapter.updateData(allNotes, allLinks));
    }
    /*
     * Показать меню действий для создания группы или заметки
     * @param anchorView Вьюха, к которой будет прикреплено меню
     */
    private void showFabActionsMenu(View anchorView) {
      /*  PopupMenu popup = new PopupMenu(this, anchorView);
        popup.inflate(R.menu.menu_fab_actions);
        // Скрыть пункт "Создать заметку", если нет групп
        if (groupAdapter.getItemCount() == 0) {
            popup.getMenu().findItem(R.id.menu_create_note).setVisible(false);
        }
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_create_group) {
                showCreateGroupDialog();
                return true;
            } else if (id == R.id.menu_create_note) {
                // Переход к созданию новой заметки
                Intent intent = new Intent(this, NoteEditActivity.class);
                intent.putExtra("mode", "create");
                startActivity(intent);
                return true;
            }
            return false;
        });

        popup.show();
    */}
    /*
     * Показать диалог для создания новой группы
     */
    private void showCreateGroupDialog() {
      /*  View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_group, null);
        // Настройка элементов диалога
        dialogView.findViewById(R.id.buttonUpdate).setVisibility(View.GONE);
        dialogView.findViewById(R.id.buttonDelete).setVisibility(View.GONE);
        //
        EditText editTextGroupName = dialogView.findViewById(R.id.editTextGroupName);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        // Обработчики
        dialogView.findViewById(R.id.buttonCreate).setOnClickListener(v -> {
            String groupName = editTextGroupName.getText().toString().trim();
            if (!groupName.isEmpty()) {
                groupService.createGroup(new Group.GroupDTO(groupName), new GroupService.OperationCallback() {
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
        });

        dialogView.findViewById(R.id.buttonCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
  */  }
}
