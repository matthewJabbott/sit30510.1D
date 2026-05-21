package com.example.sit305_101d;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sit305_101d.databinding.ActivityInterestsBinding;
import com.google.android.material.chip.Chip;

public class InterestsActivity extends AppCompatActivity {
    private ActivityInterestsBinding binding;
    private DatabaseHelper db;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInterestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME_KEY");

        setupInterestChips();
        binding.btnNext.setOnClickListener(v -> saveAndReturnToLogin());
    }

    private void setupInterestChips() {
        String[] topics = {"Algorithms", "Data Structures", "Web Development", "Testing",
                "Artificial Intelligence", "Cybersecurity", "Cloud Computing",
                "Mobile Dev", "Database Design", "DevOps", "Rust Systems", "UI/UX"};

        for (String topic : topics) {
            Chip chip = new Chip(this);
            chip.setText(topic);
            chip.setCheckable(true);
            binding.cgInterests.addView(chip);
        }
    }

    private void saveAndReturnToLogin() {
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Session error. Please login.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder selected = new StringBuilder();
        for (int id : binding.cgInterests.getCheckedChipIds()) {
            Chip chip = findViewById(id);
            selected.append(chip.getText().toString()).append(",");
        }

        db.updateUserProfile(username, selected.toString(), "Beginner");

        // Set session so MainActivity knows who just registered
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        preferences.edit().putString("current_user", username).apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}