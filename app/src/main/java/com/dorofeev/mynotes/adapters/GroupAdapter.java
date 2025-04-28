package com.dorofeev.mynotes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.PopupMenu;

import com.dorofeev.mynotes.R;
import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.services.GroupService;
import com.dorofeev.mynotes.services.ServiceLocator;

import java.util.List;

/**
 * Адаптер для отображения групп заметок
 */
public class GroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_GROUP = 0;
    private static final int VIEW_TYPE_ADD = 1;

    private List<Group> groups;

    public GroupAdapter(List<Group> groups) {
        this.groups = groups;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_GROUP) {
            View view = inflater.inflate(R.layout.item_group, parent, false);
            return new GroupViewHolder(view);
        } else { // VIEW_TYPE_ADD
            View view = inflater.inflate(R.layout.item_add_group, parent, false);
            return new AddGroupViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position){
        if (getItemViewType(position) == VIEW_TYPE_GROUP) {
            Group group = groups.get(position);
            GroupViewHolder groupHolder = (GroupViewHolder) holder;
            groupHolder.textViewGroupName.setText(group.getName());

            groupHolder.buttonGroupMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), groupHolder.buttonGroupMenu);
                popupMenu.inflate(R.menu.menu_group);

                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_add_note) {
                        Toast.makeText(v.getContext(), "Добавить заметку для " + group.getName(), Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (itemId == R.id.menu_edit_group) {
                        Toast.makeText(v.getContext(), "Редактировать группу " + group.getName(), Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (itemId == R.id.menu_delete_group) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Удаление группы")
                                .setMessage("Вы уверены, что хотите удалить группу \"" + group.getName() + "\"?")
                                .setPositiveButton("Да", (dialog, which) -> {
                                    ServiceLocator.getGroupService().deleteGroup(group.getId(), new GroupService.OperationCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(v.getContext(), "Группа удалена", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Toast.makeText(v.getContext(), "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                })
                                .setNegativeButton("Нет", (dialog, which) -> dialog.dismiss())
                                .show();
                        return true;
                    } else {
                        return false;
                    }
                });

                popupMenu.show();
            });
        } else if (getItemViewType(position) == VIEW_TYPE_ADD) {
            AddGroupViewHolder addHolder = (AddGroupViewHolder) holder;
            addHolder.itemView.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Создание новой группы", Toast.LENGTH_SHORT).show();
                // Здесь потом откроем диалог создания группы
            });
        }
    }

    @Override
    public int getItemCount() {
        return groups.size() + 1; // плюс одна карточка для добавления
    }

    @Override
    public int getItemViewType(int position) {
        if (position == groups.size()) {
            return VIEW_TYPE_ADD; // Последний элемент — добавить группу
        } else {
            return VIEW_TYPE_GROUP;
        }
    }
    /**
     * Обновить список групп (например, после поиска)
     */
    public void updateData(List<Group> newGroups) {
        this.groups = newGroups;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder для карточки группы
     */
    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGroupName;
        ImageButton buttonGroupMenu;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGroupName = itemView.findViewById(R.id.textViewGroupName);
            buttonGroupMenu = itemView.findViewById(R.id.buttonGroupMenu);
        }
    }
    /**
     * ViewHolder для кнопки добавления группы
     */
    static class AddGroupViewHolder extends RecyclerView.ViewHolder {
        public AddGroupViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
