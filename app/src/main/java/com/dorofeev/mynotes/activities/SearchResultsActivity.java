package com.dorofeev.mynotes.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dorofeev.mynotes.R;
import com.dorofeev.mynotes.adapters.NoteAdapter;
import com.dorofeev.mynotes.models.Note;

import java.util.List;

/*
 * Активность отображения результатов поиска
 */
public class SearchResultsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;  // Список резултата
    private NoteAdapter noteAdapter;    // Адаптер для списка заметок
    private TextView textViewQuery;     // Текстовое поле для отображения строки поиска
    private ImageButton buttonClose;    // Кнопка закрытия экрана

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // Привязка визуальных компонентов
        recyclerView = findViewById(R.id.recyclerViewResults);
        textViewQuery = findViewById(R.id.textViewQuery);
        buttonClose = findViewById(R.id.buttonClose);

        // Настройка списка заметок
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter();
        recyclerView.setAdapter(noteAdapter);

        // Получение данных из Intent
        String query = getIntent().getStringExtra("query");
        List<Note> notes = (List<Note>) getIntent().getSerializableExtra("notes");

        // Отображение строки поиска и списка
        textViewQuery.setText("Результат поиска: \"" + query + "\"");

        if (notes != null && !notes.isEmpty()) {
            noteAdapter.setEditable(false);
            noteAdapter.setNotes(notes);
        }

        // Закрытие экрана по нажатию X
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
