package com.ergasia.minty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private FirebaseAuth mAuth;

    private Button loginButton, registerButton;

    private AppDatabase db;


    private static final String TAG = "LoginActivity"; // Tag for logs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.editEmailProfile);
        passwordInput = findViewById(R.id.editPasswordLogin);

        loginButton = findViewById(R.id.loginButton);

        registerButton = findViewById(R.id.registerBtn);

        loginButton.setOnClickListener(v -> {
                    String email = emailInput.getText().toString();
                    String password = passwordInput.getText().toString();

                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(this,RegisterActivity.class));
        });
    }
}