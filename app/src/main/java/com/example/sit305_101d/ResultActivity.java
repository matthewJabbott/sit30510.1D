package com.example.sit305_101d;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sit305_101d.databinding.ActivityResultBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {
    private ActivityResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int score = getIntent().getIntExtra("SCORE", 0);
        int total = getIntent().getIntExtra("TOTAL", 5);
        String topicFromIntent = getIntent().getStringExtra("TOPIC");

        if (topicFromIntent == null || topicFromIntent.isEmpty()) {
            topicFromIntent = getString(R.string.default_topic_name);
        }

        final int finalScore = score;
        final int finalTotal = total;
        final String finalTopic = topicFromIntent;

        binding.scoreSummary.setText(getString(R.string.score_summary_format, finalScore, finalTotal));

        // Save to history immediately
        String currentUserName = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("current_user", "");
        try (DatabaseHelper db = new DatabaseHelper(this)) {
            db.insertQuizHistory(currentUserName, finalTopic, finalScore, finalTotal);
        }

        binding.btnExplain.setOnClickListener(viewExplain -> {
            binding.reviewText.setText(R.string.ai_analyzing);
            String baseUrl = "http://192.168.0.210:8080/";
            QuizService service = RetrofitClient.getClient(baseUrl).create(QuizService.class);
            QuizService.ExplanationRequest request = new QuizService.ExplanationRequest(finalScore, finalTotal, finalTopic);

            service.explainResults(request).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<QuizService.ExplanationResponse> call, @NonNull Response<QuizService.ExplanationResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        binding.reviewText.setText(response.body().getExplanation());
                    } else if (response.code() == 404) {
                        binding.reviewText.setText(R.string.error_404_endpoint);
                    } else {
                        binding.reviewText.setText(getString(R.string.server_error, response.code()));
                    }
                }
                @Override
                public void onFailure(@NonNull Call<QuizService.ExplanationResponse> call, @NonNull Throwable t) {
                    binding.reviewText.setText(getString(R.string.connection_failed_detailed, t.getMessage()));
                }
            });
        });

        binding.btnHome.setOnClickListener(viewHome -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}