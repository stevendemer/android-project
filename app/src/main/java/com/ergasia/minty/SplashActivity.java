package com.ergasia.minty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // check if user is logged in
        SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        int userId = preferences.getInt("user_id", -1);

        Intent intent;

        if (userId != -1) {
            intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("user_id", userId);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }

}