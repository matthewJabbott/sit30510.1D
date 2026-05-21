package com.example.sit305_101d;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sit305_101d.databinding.ActivitySetupBinding;

public class SetupActivity extends AppCompatActivity {
    private ActivitySetupBinding binding;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialize Binding
        binding = ActivitySetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            db = new DatabaseHelper(this);

            // Get the current user from session to check profile status
            String currentUserName = getCurrentUserName();

            if (db.getUser(currentUserName) != null) {
                User user = db.getUser(currentUserName);
                // If they already have interests, skip setup
                if (user.getInterests() != null && !user.getInterests().isEmpty()) {
                    navigateToMain();
                    return;
                }
            }

            // 2. Handle Save Logic
            binding.saveBtn.setOnClickListener(v -> {
                String nameInput = binding.nameEdit.getText().toString().trim();

                if (nameInput.isEmpty()) {
                    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Collect interests from checkboxes
                StringBuilder interests = new StringBuilder();
                if (binding.checkAI.isChecked()) interests.append("AI, ");
                if (binding.checkMobile.isChecked()) interests.append("Mobile, ");
                if (binding.checkCloud.isChecked()) interests.append("Cloud, ");

                String finalInterests = interests.toString().trim();
                // Remove trailing comma if exists
                if (finalInterests.endsWith(",")) {
                    finalInterests = finalInterests.substring(0, finalInterests.length() - 1);
                }

                if (finalInterests.isEmpty()) finalInterests = "General IT";

                try {
                    db.updateUserProfile(currentUserName, finalInterests, "Beginner");

                    Intent intent = new Intent(SetupActivity.this, TopicSelectionActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e("DB_ERROR", "Update failed: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e("CRITICAL_ERROR", "Setup failed to load: " + e.getMessage());
        }
    }

    private String getCurrentUserName() {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return preferences.getString("current_user", "");
    }

    private void navigateToMain() {
        Intent intent = new Intent(SetupActivity.this, TopicSelectionActivity.class);
        startActivity(intent);
        finish();
    }
}