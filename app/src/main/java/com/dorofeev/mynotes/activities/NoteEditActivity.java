package com.dorofeev.mynotes.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dorofeev.mynotes.R;
import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.models.Note;
import com.dorofeev.mynotes.services.GroupService;
import com.dorofeev.mynotes.services.NoteService;
import com.dorofeev.mynotes.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

public class NoteEditActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent, editTextTags, editTextImageUrl, editTextFileUrls;
    private Spinner spinnerGroup;

    private final NoteService noteService = ServiceLocator.getNoteService();
    private final GroupService groupService = ServiceLocator.getGroupService();

    private List<Group> groups = new ArrayList<>();
    private String selectedGroupId = null;

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
        String mode = getIntent().getStringExtra("mode");
        boolean isEditMode = "edit".equals(mode);
        Button buttonDelete = findViewById(R.id.buttonDelete);
        buttonDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        findViewById(R.id.buttonCancel).setOnClickListener(v -> finish());
        findViewById(R.id.buttonSave).setOnClickListener(v -> saveNote());

        loadGroups();
    }

    private void loadGroups() {
        groupService.startListeningGroupChange(new GroupService.GroupsChangedListener() {
            @Override
            public void onGroupsChanged(List<Group> loadedGroups) {
                groups = loadedGroups;
                List<String> groupNames = new ArrayList<>();
                for (Group g : groups) {
                    groupNames.add(g.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(NoteEditActivity.this,
                        android.R.layout.simple_spinner_item, groupNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGroup.setAdapter(adapter);

                spinnerGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedGroupId = groups.get(position).getId();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedGroupId = null;
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(NoteEditActivity.this, "Ошибка загрузки групп", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();
        String tagsInput = editTextTags.getText().toString().trim();
        String fileUrlsInput = editTextFileUrls.getText().toString().trim();

        List<String> tags = new ArrayList<>();
        if (!tagsInput.isEmpty()) {
            for (String tag : tagsInput.split(",")) {
                tags.add(tag.trim());
            }
        }

        List<String> fileUrls = new ArrayList<>();
        if (!fileUrlsInput.isEmpty()) {
            for (String url : fileUrlsInput.split(";")) {
                if (!url.trim().isEmpty()) {
                    fileUrls.add(url.trim());
                }
            }
        }

        if (title.isEmpty() || selectedGroupId == null) {
            Toast.makeText(this, "Введите заголовок и выберите группу", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUrl.isEmpty()) imageUrl = null;

        Note note = new Note(title, content, selectedGroupId, imageUrl, fileUrls, tags);

        noteService.createNote(note, new NoteService.OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(NoteEditActivity.this, "Заметка создана", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(NoteEditActivity.this, "Ошибка создания: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
