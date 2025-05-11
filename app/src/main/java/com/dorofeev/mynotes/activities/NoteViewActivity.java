package com.dorofeev.mynotes.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dorofeev.mynotes.R;
import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.models.Note;
import com.dorofeev.mynotes.services.GroupNoteManager;
import com.squareup.picasso.Picasso;
import io.noties.markwon.Markwon;

import java.util.Map;

public class NoteViewActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textViewTitleGroup, textViewGroup, textViewTags, textViewContent, textViewFiles;
    private ImageButton buttonClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);
        imageView = findViewById(R.id.imageView);
        textViewTitleGroup = findViewById(R.id.textViewTitleGroup);
        textViewGroup = findViewById(R.id.textViewGroup);
        textViewTags = findViewById(R.id.textViewTags);
        textViewContent = findViewById(R.id.textViewContent);
        textViewFiles = findViewById(R.id.textViewFiles);
        buttonClose = findViewById(R.id.buttonClose);

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        String noteId = getIntent().getStringExtra("noteId");
        if (noteId == null) return;

        GroupNoteManager manager = GroupNoteManager.getInstance();
        Map<Group, java.util.List<Note>> structure = manager.getStructureSnapshot();

        for (Map.Entry<Group, java.util.List<Note>> entry : structure.entrySet()) {
            for (Note note : entry.getValue()) {
                if (note.getId().equals(noteId)) {
                    loadNote(note, entry.getKey());
                    return;
                }
            }
        }
    }

    private void loadNote(Note note, Group group) {
        if (note.getImageUrl() != null && !note.getImageUrl().isEmpty()) {
            Log.d("PicassoDebug", "Загружаю изображение: " + note.getImageUrl());

            Picasso.get()
                    .load(note.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("PicassoDebug", "Изображение успешно загружено");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("PicassoDebug", "Ошибка загрузки изображения", e);
                        }
                    });
        } else {
            Log.d("PicassoDebug", "URL отсутствует, показываем заглушку");
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        textViewTitleGroup.setText(note.getTitle());
        textViewGroup.setText(group.getName());
        textViewTags.setText(String.join(", ", note.getTags()));

        // Рендерим Markdown через Markwon
        Markwon markwon = Markwon.create(this);
        markwon.setMarkdown(textViewContent, note.getContentMarkdown());

        // Файлы: по строкам, ссылки кликабельны
        textViewFiles.setText(String.join("\n", note.getFileUrls()));
    }
}
