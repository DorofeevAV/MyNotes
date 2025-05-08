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

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final List<Note> notes = new ArrayList<>();

    public void setNotes(List<Note> newNotes) {
        notes.clear();
        notes.addAll(newNotes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

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
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NoteViewActivity.class);
            intent.putExtra("noteId", note.getId());
            context.startActivity(intent);
        });

        // Кнопка редактирования заметки
        holder.buttonEditNote.setOnClickListener(v -> {
            Intent intent = new Intent(context, NoteEditActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("noteId", note.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewNote;
        TextView textViewNoteTitle;
        ImageButton buttonEditNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewNote = itemView.findViewById(R.id.imageViewNote);
            textViewNoteTitle = itemView.findViewById(R.id.textViewNoteTitle);
            buttonEditNote = itemView.findViewById(R.id.buttonEditNote);
        }
    }
}
