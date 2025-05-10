package com.dorofeev.mynotes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dorofeev.mynotes.R;
import com.dorofeev.mynotes.models.User;
import com.dorofeev.mynotes.services.LoginService;

import java.util.ArrayList;
import java.util.List;

/*
 * Вход в приложение
 */
public class LoginActivity extends AppCompatActivity {

    private ProgressBar progressBar;    // компонент - индикатор загрузки
    private Spinner userSpinner;        // выпадающий список пользователей
    private Button loginButton;         // кнопка входа
    private final List<User> userList = new ArrayList<>();  // список пользователей
    // Перегружаем метод onCreate для инициализации элементов интерфейса
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Инициализация элементов интерфейса
        progressBar = findViewById(R.id.progressBar);
        userSpinner = findViewById(R.id.userSpinner);
        loginButton = findViewById(R.id.buttonLogin);
        // Установка обработчиков
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginClick();
            }
        });
        // Загружаем пользователей
        loadUsers();
    }
    // Метод для загрузки пользователей
    private void loadUsers() {
        // Отображаем ProgressBar, пока загружаем пользователей, все кнопки и список не активны
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
        userSpinner.setEnabled(false);
        // Получаем список пользователей из LoginService
        LoginService.getInstance().getUsers(new LoginService.UsersLoadedCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                // Список пользователей загружен
                progressBar.setVisibility(View.GONE);
                userList.clear();
                userList.addAll(users);

                if (users.isEmpty()) {
                    // Если нет доступных пользователей, отключаем Spinner и кнопку входа
                    userSpinner.setEnabled(false);
                    loginButton.setEnabled(false);
                    // Показываем сообщение об ошибке
                    Toast.makeText(LoginActivity.this, "Нет доступных пользователей", Toast.LENGTH_LONG).show();
                    return;
                }
                // Заполняем Spinner списком пользователей
                List<String> usernames = new ArrayList<>();
                for (User user : users) {
                    usernames.add(user.getUsername());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        LoginActivity.this,
                        android.R.layout.simple_spinner_item,
                        usernames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                userSpinner.setAdapter(adapter);
                // Включаем Spinner и кнопку входа
                loginButton.setEnabled(true);
                userSpinner.setEnabled(true);
            }

            @Override
            public void onError(Exception e) {
                // Ошибка загрузки пользователей
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Ошибка загрузки пользователей", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Метод для обработки нажатия кнопки входа
    private void onLoginClick() {
        //
        int selectedIndex = userSpinner.getSelectedItemPosition();
        User selectedUser = userList.get(selectedIndex);
        userSpinner.setEnabled(false);
        loginButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        LoginService.getInstance().loginUser(selectedUser.getId(), new LoginService.UserLoginCallback() {
            @Override
            public void onUserLoggedIn(User loggedInUser) {
                // Успешный вход
                Toast.makeText(LoginActivity.this, "Вы вошли как: " + loggedInUser.getUsername(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                // Переход на главный экран приложения
                startActivity(intent);
                finish();
            }
            @Override
            public void onError(Exception e) {
                // Ошибка входа
                Toast.makeText(LoginActivity.this, "Ошибка входа: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                userSpinner.setEnabled(true);
                loginButton.setVisibility(View.VISIBLE);
            }
        });
    }
}
