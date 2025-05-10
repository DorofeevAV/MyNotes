package com.dorofeev.mynotes.adapters;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dorofeev.mynotes.R;
import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.models.Note;
import com.dorofeev.mynotes.services.GroupNoteManager;
import com.dorofeev.mynotes.services.GroupService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Адаптер для отображения групп с вложенными заметками
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private final List<Group> groups = new ArrayList<>(); // Список групп
    private final Map<String, List<Note>> groupNotesMap = new HashMap<>(); // Карта групп и их заметок
    /*
     * Создание  нового списка групп и их заметок
     */
    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }
    /*
     * Привязка данных к элементу списка
     */
    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.textViewGroupName.setText(group.getName());

        List<Note> notes = groupNotesMap.containsKey(group.getId())
                ? groupNotesMap.get(group.getId())
                : new ArrayList<>();
        holder.noteAdapter.setNotes(notes);
        // Установка слушателя нажатия на элемент списка заметок
        holder.buttonEditGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditGroupDialog(v, group);
            }
        });
    }
    // Возвращает количество элементов в списке
    @Override
    public int getItemCount() {
        return groups.size();
    }

    /**
     * Обновить отображаемые данные на основе новой структуры
     */
    public void updateData(Map<Group, List<Note>> structure) {
        groups.clear();
        groupNotesMap.clear();

        for (Map.Entry<Group, List<Note>> entry : structure.entrySet()) {
            groups.add(entry.getKey());
            groupNotesMap.put(entry.getKey().getId(), entry.getValue());
        }

        notifyDataSetChanged();
    }

    /**
     * Показать диалог для редактирования или удаления группы
     */
    private void showEditGroupDialog(final View view, final Group group) {
        // Создание диалогового окна
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_group, null);
        dialogView.findViewById(R.id.buttonCreate).setVisibility(View.GONE);
        dialogView.findViewById(R.id.buttonUpdate).setVisibility(View.VISIBLE);
        dialogView.findViewById(R.id.buttonDelete).setVisibility(View.VISIBLE);
        // Установка заголовка диалога
        final EditText editTextGroupName = dialogView.findViewById(R.id.editTextGroupName);
        editTextGroupName.setText(group.getName());
        // Создание диалога
        final AlertDialog dialog = new AlertDialog.Builder(view.getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        // Установка слушателя нажатия на кнопку "Обновить"
        dialogView.findViewById(R.id.buttonUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = editTextGroupName.getText().toString().trim();
                if (!newName.isEmpty()) {
                    dialog.dismiss();
                    Group updatedGroup = new Group(group);
                    updatedGroup.setName(newName);
                    GroupNoteManager.getInstance().updateGroup(updatedGroup, new GroupService.OperationCallback() {
                        @Override
                        public void onSuccess(Group result) {
                            Toast.makeText(view.getContext(), "Группа обновлена", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(view.getContext(), "Ошибка обновления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(view.getContext(), "Имя группы не должно быть пустым", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Установка слушателя нажатия на кнопку "Удалить"
        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Удаление группы")
                        .setMessage("Вы уверены, что хотите удалить группу \"" + group.getName() + "\"?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                GroupNoteManager.getInstance().deleteGroup(group.getId(), new GroupService.DeleteCallback() {
                                    @Override
                                    public void onSuccess(String groupId) {
                                        Toast.makeText(view.getContext(), "Группа удалена", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(view.getContext(), "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                d.dismiss();
                            }
                        })
                        .show();
            }
        });
        // Установка слушателя нажатия на кнопку "Отмена"
        dialogView.findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // Показать диалог
        dialog.show();
    }


    /**
     * ViewHolder для карточки группы
     */
    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGroupName; // Название группы
        ImageButton buttonEditGroup; // Кнопка редактирования группы
        RecyclerView recyclerViewNotes; // Список заметок в группе
        NoteAdapter noteAdapter; // Адаптер для заметок
        // Конструктор для создания элемента списка групп
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGroupName = itemView.findViewById(R.id.textViewGroupName);
            buttonEditGroup = itemView.findViewById(R.id.buttonEditGroup);
            recyclerViewNotes = itemView.findViewById(R.id.recyclerViewNotes);
            recyclerViewNotes.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            noteAdapter = new NoteAdapter();
            recyclerViewNotes.setAdapter(noteAdapter);
        }
    }
}
