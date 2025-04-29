package com.dorofeev.mynotes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.dorofeev.mynotes.R;
import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.services.GroupService;
import com.dorofeev.mynotes.services.ServiceLocator;

import java.util.List;

/**
 * Адаптер для отображения групп заметок
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groups;

    public GroupAdapter(List<Group> groups) {
        this.groups = groups;
    }

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

        holder.buttonEditGroup.setOnClickListener(v -> showEditGroupDialog(v, group));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void updateData(List<Group> newGroups) {
        this.groups = newGroups;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder для карточки группы
     */
    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGroupName;
        ImageButton buttonEditGroup;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGroupName = itemView.findViewById(R.id.textViewGroupName);
            buttonEditGroup = itemView.findViewById(R.id.buttonEditGroup);
        }
    }

    /**
     * Показать диалог для редактирования или удаления группы
     */
    private void showEditGroupDialog(View view, Group group) {
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_edit_group, null);
        EditText editTextGroupName = dialogView.findViewById(R.id.editTextGroupName);
        Button buttonUpdate = dialogView.findViewById(R.id.buttonUpdate);
        Button buttonDelete = dialogView.findViewById(R.id.buttonDelete);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        editTextGroupName.setText(group.getName());

        AlertDialog dialog = new AlertDialog.Builder(view.getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        buttonUpdate.setOnClickListener(v -> {
            String newName = editTextGroupName.getText().toString().trim();
            if (!newName.isEmpty()) {
                group.setName(newName);
                ServiceLocator.getGroupService().updateGroup(group, new GroupService.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(view.getContext(), "Группа обновлена", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
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

        buttonDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("Удаление группы")
                    .setMessage("Вы уверены, что хотите удалить группу \"" + group.getName() + "\"?")
                    .setPositiveButton("Да", (d, which) -> {
                        ServiceLocator.getGroupService().deleteGroup(group.getId(), new GroupService.OperationCallback() {
                            @Override
                            public void onSuccess() {
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

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

}
