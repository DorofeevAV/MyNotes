package com.dorofeev.mynotes.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dorofeev.mynotes.R;
import com.dorofeev.mynotes.activities.NoteEditActivity;
import com.dorofeev.mynotes.activities.NoteViewActivity;
import com.dorofeev.mynotes.models.Note;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/*
 * Адаптер для отображения списка заметок
 * Использует RecyclerView для отображения элементов списка
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final List<Note> notes = new ArrayList<>(); // Список заметок
    private boolean editable = true; // Флаг редактирования
    /*
     * Установка нового списка заметок
     * @param newNotes Новый список заметок
     */
    public void setNotes(List<Note> newNotes) {
        notes.clear();
        notes.addAll(newNotes);
        notifyDataSetChanged();
    }
    /*
     * Установка флага редактирования
     * @param editable Флаг редактирования
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    // Возвращает количество элементов в списке
    @Override
    public int getItemCount() {
        return notes.size();
    }
    /*
     * Создание нового элемента списка заметок
     */
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }
    /*
     * Привязка данных к элементу списка
     */
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        Context context = holder.itemView.getContext();

        holder.textViewNoteTitle.setText(note.getTitle());

        // Загрузка изображения через Picasso
        if (note.getImageUrl() != null && !note.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(note.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .resize(100, 100)
                    .centerCrop()
                    .into(holder.imageViewNote);
        } else {
            holder.imageViewNote.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Открыть экран просмотра заметки
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, NoteViewActivity.class);
                intent.putExtra("noteId", note.getId());
                context.startActivity(intent);
            }
        });

        // Управление кнопкой редактирования
        if (editable) {
            holder.buttonEditNote.setVisibility(View.VISIBLE);
            holder.buttonEditNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, NoteEditActivity.class);
                    intent.putExtra("mode", "edit");
                    intent.putExtra("noteId", note.getId());
                    context.startActivity(intent);
                }
            });
        } else {
            holder.buttonEditNote.setVisibility(View.GONE);
            holder.buttonEditNote.setOnClickListener(null);
        }
    }
    /*
     * Внутренний класс для представления элемента списка заметок
     */
    static class NoteViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewNote; // Изображение заметки
        TextView textViewNoteTitle; // Заголовок заметки
        ImageButton buttonEditNote; // Кнопка редактирования заметки
        /*
         * Конструктор для создания элемента списка заметок
         * @param itemView Элемент списка
         */
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewNote = itemView.findViewById(R.id.imageViewNote);
            textViewNoteTitle = itemView.findViewById(R.id.textViewNoteTitle);
            buttonEditNote = itemView.findViewById(R.id.buttonEditNote);
        }
    }
}
