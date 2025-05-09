package com.ergasia.minty;

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

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput, firstNameInput, lastNameInput, emailInput;

    private Button registerButton;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "minty-db").allowMainThreadQueries().build();

        usernameInput = findViewById(R.id.editUsername);
        emailInput = findViewById(R.id.editEmailAddress);
        passwordInput = findViewById(R.id.editPassword);
        firstNameInput = findViewById(R.id.editFirstName);
        lastNameInput = findViewById(R.id.editLastName);

        registerButton = findViewById(R.id.registerBtn);


        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();
            String firstName = firstNameInput.getText().toString();
            String lastName = lastNameInput.getText().toString();
            String email = emailInput.getText().toString();


            if (!username.isEmpty() && !password.isEmpty()) {
                User existingUser = db.userDao().findByUsername(username);
                if (existingUser == null) {
                    db.userDao().registerUser(new User(firstName, lastName, username, email, password));
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                    finish(); // go back to login
                } else {
                    Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            }

        });


    }
}