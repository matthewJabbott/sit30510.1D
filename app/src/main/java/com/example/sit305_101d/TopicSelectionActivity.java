package com.example.sit305_101d;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.sit305_101d.databinding.ActivityTopicSelectionBinding;

public class TopicSelectionActivity extends AppCompatActivity {
    private ActivityTopicSelectionBinding binding;
    private DatabaseHelper db;
    private String selectedTopic = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTopicSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new DatabaseHelper(this);
        binding.gridLayoutTopics.removeAllViews();
        loadDynamicTopics();

        binding.btnConfirmTopic.setOnClickListener(v -> {
            if (selectedTopic.isEmpty()) {
                Toast.makeText(this, "Please select a topic", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().putString("session_topic", selectedTopic).apply();

            Intent intent = new Intent(this, LessonActivity.class);
            intent.putExtra("SELECTED_TASK", selectedTopic);
            startActivity(intent);
            finish();
        });

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void loadDynamicTopics() {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = preferences.getString("current_user", "");
        User user = db.getUser(name);

        if (user != null && user.getInterests() != null) {
            String[] interests = user.getInterests().split(",");
            for (String interest : interests) {
                String clean = interest.trim();
                if (!clean.isEmpty()) createTopicCard(clean);
            }
        }
    }

    private void createTopicCard(final String topicName) {
        CardView card = new CardView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = (int) (100 * getResources().getDisplayMetrics().density);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(15, 15, 15, 15);
        card.setLayoutParams(params);
        card.setRadius(25f);
        card.setCardElevation(10f);

        TextView tv = new TextView(this);
        tv.setText(topicName);
        tv.setGravity(Gravity.CENTER);
        card.addView(tv);

        card.setOnClickListener(v -> {
            for (int i = 0; i < binding.gridLayoutTopics.getChildCount(); i++) {
                binding.gridLayoutTopics.getChildAt(i).setAlpha(1.0f);
            }
            v.setAlpha(0.5f);
            selectedTopic = topicName;
            binding.btnConfirmTopic.setEnabled(true);
        });
        binding.gridLayoutTopics.addView(card);
    }
}