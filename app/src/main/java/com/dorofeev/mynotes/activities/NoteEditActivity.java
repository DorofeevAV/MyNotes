package com.dorofeev.mynotes.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dorofeev.mynotes.R;
import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.models.Note;
import com.dorofeev.mynotes.services.GroupNoteManager;

import java.util.*;

public class NoteEditActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent, editTextTags, editTextImageUrl, editTextFileUrls;
    private Spinner spinnerGroup;
    private Button buttonDelete;

    private final GroupNoteManager manager = GroupNoteManager.getInstance();
    private List<Group> groups = new ArrayList<>();
    private Group selectedGroup = null;
    private Note editingNote = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        editTextTags = findViewById(R.id.editTextTags);
        editTextImageUrl = findViewById(R.id.editTextImageUrl);
        editTextFileUrls = findViewById(R.id.editTextFileUrls);
        spinnerGroup = findViewById(R.id.spinnerGroup);
        buttonDelete = findViewById(R.id.buttonDelete);

        String mode = getIntent().getStringExtra("mode");
        boolean isEditMode = "edit".equals(mode);

        if (isEditMode) {
            String noteId = getIntent().getStringExtra("noteId");
            for (Group group : manager.getStructureSnapshot().keySet()) {
                for (Note note : manager.getStructureSnapshot().get(group)) {
                    if (note.getId().equals(noteId)) {
                        editingNote = new Note(note);
                        selectedGroup = group;
                        break;
                    }
                }
                if (editingNote != null) break;
            }
            if (editingNote != null) fillForm(editingNote);
        }

        buttonDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editingNote != null) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(NoteEditActivity.this);
                    builder.setTitle("Удаление заметки");
                    builder.setMessage("Вы уверены, что хотите удалить эту заметку?");
                    builder.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            manager.deleteNote(editingNote.getId(), newDeleteCallback("Удалено"));
                        }
                    });
                    builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss(); // просто закрываем диалог
                        }
                    });
                    builder.show();
                }
            }
        });

        findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote(isEditMode);
            }
        });

        loadGroups();
    }

    private void fillForm(Note note) {
        editTextTitle.setText(note.getTitle());
        editTextContent.setText(note.getContentMarkdown());
        editTextTags.setText(String.join(";", note.getTags()));
        editTextImageUrl.setText(note.getImageUrl());
        editTextFileUrls.setText(String.join(";", note.getFileUrls()));
    }

    private void loadGroups() {
        groups = new ArrayList<>(manager.getStructureSnapshot().keySet());
        List<String> groupNames = new ArrayList<>();
        for (Group g : groups) groupNames.add(g.getName());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroup.setAdapter(adapter);

        spinnerGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = groups.get(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {
                selectedGroup = null;
            }
        });

        if (selectedGroup != null) {
            int index = groups.indexOf(selectedGroup);
            if (index >= 0) spinnerGroup.setSelection(index);
        }
    }

    private void saveNote(boolean isEditMode) {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();
        List<String> tags = Arrays.asList(editTextTags.getText().toString().trim().split(";"));
        List<String> fileUrls = Arrays.asList(editTextFileUrls.getText().toString().trim().split(";"));

        if (title.isEmpty() || selectedGroup == null) {
            Toast.makeText(this, "Введите заголовок и выберите группу", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode && editingNote != null) {
            editingNote.setTitle(title);
            editingNote.setContentMarkdown(content);
            editingNote.setImageUrl(imageUrl);
            editingNote.setTags(tags);
            editingNote.setFileUrls(fileUrls);
            manager.updateNote(editingNote, selectedGroup, newNoteCallback("Сохранено"));
        } else {
            manager.createNote(selectedGroup, title, content, imageUrl, fileUrls, tags, newNoteCallback("Создано"));
        }
    }

    private com.dorofeev.mynotes.services.NoteService.OperationCallback newNoteCallback(String successMessage) {
        return new com.dorofeev.mynotes.services.NoteService.OperationCallback() {
            @Override public void onSuccess(Note note) {
                Toast.makeText(NoteEditActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override public void onError(Exception e) {
                Toast.makeText(NoteEditActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private com.dorofeev.mynotes.services.NoteService.DeleteCallback newDeleteCallback(String successMessage) {
        return new com.dorofeev.mynotes.services.NoteService.DeleteCallback() {
            @Override public void onSuccess(List<String> noteIds) {
                Toast.makeText(NoteEditActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override public void onError(Exception e) {
                Toast.makeText(NoteEditActivity.this, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }
}
