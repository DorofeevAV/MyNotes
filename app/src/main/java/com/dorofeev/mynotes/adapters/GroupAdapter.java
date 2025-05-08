package com.dorofeev.mynotes.adapters;

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
import com.dorofeev.mynotes.models.GroupNoteLink;
import com.dorofeev.mynotes.models.Note;
import com.dorofeev.mynotes.services.GroupService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Адаптер для отображения групп заметок
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private final List<Group> groups = new ArrayList<Group>();
    private Map<String, List<Note>> groupNotesMap = new HashMap<>();

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.textViewGroupName.setText(group.getName());

        List<Note> notes = groupNotesMap.get(group.getId());
        if (notes == null) notes = new ArrayList<>();
        holder.noteAdapter.setNotes(notes);

        holder.buttonEditGroup.setOnClickListener(v -> showEditGroupDialog(v, group));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void updateData(List<Note> notes, List<GroupNoteLink> links) {
 /*       this.groups.clear();
        this.groups.addAll(ServiceLocator.getGroupService().getCachedGroups());

        Map<String, List<Note>> newMap = new HashMap<>();
        for (Group group : groups) newMap.put(group.getId(), new ArrayList<>());

        for (GroupNoteLink link : links) {
            for (Note note : notes) {
                if (note.getId().equals(link.getNoteId())) {
                    List<Note> list = newMap.get(link.getGroupId());
                    if (list != null) list.add(note);
                }
            }
        }

        this.groupNotesMap = newMap;

        // Простая перерисовка
        notifyDataSetChanged();
   */ }

    /*
     * Показать диалог для редактирования или удаления группы
     */
    private void showEditGroupDialog(View view, Group group) {
   /*     View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_group, null);
        // Настройка элементов диалога
        dialogView.findViewById(R.id.buttonCreate).setVisibility(View.GONE);
        //
        EditText editTextGroupName = dialogView.findViewById(R.id.editTextGroupName);
        editTextGroupName.setText(group.getName());
        GroupService groupService = ServiceLocator.getGroupService();
        //
        AlertDialog dialog = new AlertDialog.Builder(view.getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        // Обработчики
        dialogView.findViewById(R.id.buttonUpdate).setOnClickListener(v -> {
            String newName = editTextGroupName.getText().toString().trim();
            if (!newName.isEmpty()) {
                dialog.dismiss();
                Group updatedGroup = new Group(group);
                updatedGroup.setName(newName);
                groupService.updateGroup(updatedGroup, new GroupService.OperationCallback() {
                    @Override
                    public void onSuccess(Group group) {
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
        });

        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("Удаление группы")
                    .setMessage("Вы уверены, что хотите удалить группу \"" + group.getName() + "\"?")
                    .setPositiveButton("Да", (d, which) -> {
                        groupService.deleteGroup(group, new GroupService.OperationCallback() {
                            @Override
                            public void onSuccess(Group group) {
                                Toast.makeText(view.getContext(), "Группа удалена", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(view.getContext(), "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Нет", (d, which) -> d.dismiss())
                    .show();
        });

        dialogView.findViewById(R.id.buttonCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
 */   }
    /*
     * ViewHolder для карточки группы
     */
    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGroupName;
        ImageButton buttonEditGroup;

        RecyclerView recyclerViewNotes;
        NoteAdapter noteAdapter;

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
