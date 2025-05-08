package com.dorofeev.mynotes.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dorofeev.mynotes.R;
import com.dorofeev.mynotes.models.Group;
import com.dorofeev.mynotes.models.Note;
import com.dorofeev.mynotes.services.GroupNoteLinkService;
import com.dorofeev.mynotes.services.GroupService;
import com.dorofeev.mynotes.services.NoteService;

import java.util.ArrayList;
import java.util.List;

public class NoteEditActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent, editTextTags, editTextImageUrl, editTextFileUrls;
    private Spinner spinnerGroup;

    private NoteService noteService;
    private GroupService groupService;
    private GroupNoteLinkService groupNoteLinkService;

    private List<Group> groups = new ArrayList<>();
    private String selectedGroupId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);
/*        //
        noteService = ServiceLocator.getNoteService();
        groupService = ServiceLocator.getGroupService();
        groupNoteLinkService = ServiceLocator.getGroupNoteLinkService();
        //
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
*/    }

    private void loadGroups() {
/*        groupService.startListenGroups(new GroupService.GroupsChangedListener() {
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
*/    }

    private void saveNote() {
/*        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();
        String tagsInput = editTextTags.getText().toString().trim();
        String fileUrlsInput = editTextFileUrls.getText().toString().trim();

        List<String> tags = new ArrayList<>();
        if (!tagsInput.isEmpty()) {
            for (String tag : tagsInput.split(";")) {
                String t = tag.trim();
                if (!t.isEmpty()) tags.add(t);
            }
        }

        List<String> fileUrls = new ArrayList<>();
        if (!fileUrlsInput.isEmpty()) {
            for (String url : fileUrlsInput.split(";")) {
                String u = url.trim();
                if (!u.isEmpty()) fileUrls.add(u);
            }
        }

        if (title.isEmpty() || selectedGroupId == null) {
            Toast.makeText(this, "Введите заголовок и выберите группу", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUrl.isEmpty()) imageUrl = null;

        Note.NoteDTO dto = new Note.NoteDTO(title, content, imageUrl, fileUrls, tags);
        noteService.createNote(dto, new NoteService.OperationCallback() {
            @Override
            public void onSuccess(Note createdNote) {
                // При успешном создании заметки — создаём связь с группой
                GroupNoteLink.LinkDTO linkDTO = new GroupNoteLink.LinkDTO(createdNote.getId(), selectedGroupId);
                groupNoteLinkService.createLink(linkDTO, new GroupNoteLinkService.OperationCallback() {
                    @Override
                    public void onSuccess(GroupNoteLink link) {
                        Toast.makeText(NoteEditActivity.this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(NoteEditActivity.this, "Ошибка связи: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(NoteEditActivity.this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
*/    }
}
