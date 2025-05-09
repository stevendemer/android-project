package com.ergasia.minty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.ergasia.minty.entities.User;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameInput, passwordInput;

    private Button loginButton, registerButton;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "minty-db").allowMainThreadQueries().build();

        usernameInput = findViewById(R.id.editUsernameLogin);
        passwordInput = findViewById(R.id.editPasswordLogin);

        loginButton = findViewById(R.id.loginButton);

        registerButton = findViewById(R.id.registerBtn);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();

            User user = db.userDao().login(username, password);

            if (user != null) {
                // user logged in

                SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("user_id", user.getId());
                editor.apply();

                // start dashboard
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.putExtra("user_id", user.getId());
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(this,RegisterActivity.class));
        });
    }
}