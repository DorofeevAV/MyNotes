package com.dorofeev.mynotes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dorofeev.mynotes.R;
import com.dorofeev.mynotes.adapters.UserAdapter;
import com.dorofeev.mynotes.models.User;
import com.dorofeev.mynotes.services.LoginService;
import com.dorofeev.mynotes.services.ServiceLocator;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private LoginService loginService;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginService = ServiceLocator.getLoginService();
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerViewUsers);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadUsers();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);

        loginService.getUsers(new LoginService.UsersLoadedCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                progressBar.setVisibility(View.GONE);
                userAdapter = new UserAdapter(users, LoginActivity.this);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Ошибка загрузки пользователей", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUserClick(User user) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        loginService.loginUser(user.getId(), new LoginService.UserLoginCallback() {
            @Override
            public void onUserLoggedIn(User loggedInUser) {
                Toast.makeText(LoginActivity.this, "Вы вошли как: " + loggedInUser.getUsername(), Toast.LENGTH_SHORT).show();
                // Здесь переход на MainActivity или другой экран
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(LoginActivity.this, "Ошибка входа: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
}
