package com.example.sit305_101d;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sit305_101d.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private DatabaseHelper db;
    private String currentUserInterests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = new DatabaseHelper(this);

        String name = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("current_user", "");
        User user = db.getUser(name);

        if (name.isEmpty() || user == null) {
            navigateTo(LoginActivity.class);
            return;
        }

        // If NO interests set yet, force setup first
        if (user.getInterests() == null || user.getInterests().isEmpty()) {
            navigateTo(SetupActivity.class);
            return;
        }

        currentUserInterests = user.getInterests();

        binding.tvSubtitle.setText(getString(R.string.current_focus_format, currentUserInterests));
        binding.tvWelcomeTitle.setText(getString(R.string.ai_tutor_title, currentUserInterests));

        binding.btnStartQuiz.setOnClickListener(v -> {
            if (currentUserInterests.contains(",")) {
                // Multiple interests: open selector first
                startActivity(new Intent(this, TopicSelectionActivity.class));
            } else {
                // Single interest: go straight to quiz
                Intent intent = new Intent(this, LessonActivity.class);
                intent.putExtra("SELECTED_TASK", currentUserInterests);
                startActivity(intent);
            }
        });

        binding.tvSubtitle.setOnClickListener(v -> navigateTo(TopicSelectionActivity.class));
        binding.btnLogout.setOnClickListener(v -> logout());

        binding.btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        binding.btnShare.setOnClickListener(v -> startActivity(new Intent(this, ShareActivity.class)));
        binding.btnPurchase.setOnClickListener(v -> startActivity(new Intent(this, PurchaseActivity.class)));
    }

    private void navigateTo(Class<?> destination) {
        startActivity(new Intent(this, destination));
        finish();
    }

    private void logout() {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        preferences.edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}