package com.dorofeev.mynotes.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dorofeev.mynotes.R;
import com.dorofeev.mynotes.models.Note;
import com.dorofeev.mynotes.services.NoteService;
import com.squareup.picasso.Picasso;

public class NoteViewActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textViewTitleGroup, textViewTags, textViewContent, textViewFiles;
    private ImageButton buttonClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);
/*
        imageView = findViewById(R.id.imageView);
        textViewTitleGroup = findViewById(R.id.textViewTitleGroup);
        textViewTags = findViewById(R.id.textViewTags);
        textViewContent = findViewById(R.id.textViewContent);
        textViewFiles = findViewById(R.id.textViewFiles);
        buttonClose = findViewById(R.id.buttonClose);

        buttonClose.setOnClickListener(v -> finish());

        String noteId = getIntent().getStringExtra("noteId");
        if (noteId != null) {
            NoteService noteService = ServiceLocator.getNoteService();
            Note note = noteService.getNoteById(noteId); // можно реализовать кеш

            if (note != null) {
                loadNote(note);
            }
        }
*/    }

    private void loadNote(Note note) {
 /*       Picasso.get().load(note.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(imageView);

        String groupName = ServiceLocator.getGroupService().getGroupNameByNoteId(note.getId());
        textViewTitleGroup.setText(note.getTitle() + " — " + groupName);

        textViewTags.setText("Теги: " + String.join(", ", note.getTags()));
        textViewContent.setText(note.getContent());
        textViewFiles.setText("Файлы:\n" + String.join("\n", note.getFileUrls()));
*/    }
}
