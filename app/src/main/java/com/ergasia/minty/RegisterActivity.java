package com.ergasia.minty;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private Button registerButton;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.editEmailProfile);
        registerButton = findViewById(R.id.registerBtn);
        passwordInput = findViewById(R.id.editPassword);
        usernameInput = findViewById(R.id.editUsernameProfile);
        loginButton = findViewById(R.id.loginButton);

        registerButton.setOnClickListener(v -> {
            handleRegister();
        });

        // redirect to the login page
        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    private void createUserDocument(FirebaseUser user, String username) {
        Map<String, Object> userDoc = new HashMap<>();

        userDoc.put("email", user.getEmail());
        userDoc.put("username", username);
        userDoc.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users").document(user.getUid()).set(userDoc).addOnSuccessListener(v -> Log.d(TAG, "User profile created")).addOnFailureListener(e -> Log.w(TAG, "Error creating user profile" + e.toString()));
    }

    private void handleRegister() {
         String username = usernameInput.getText().toString().trim();
         String email = emailInput.getText().toString().trim();
         String password = passwordInput.getText().toString().trim();

         if (username.isEmpty()) {
             usernameInput.setError("Username is required");
             return;
         }
         if (email.isEmpty()) {
             emailInput.setError("Email is required");
             return;
         }
         if (password.isEmpty()) {
             passwordInput.setError("Password is required");
             return;
         }
         if (password.length() < 5) {
             passwordInput.setError("Password must be at least 5 characters");
             return;
         }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    createUserDocument(user, username);
                    startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "User is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}