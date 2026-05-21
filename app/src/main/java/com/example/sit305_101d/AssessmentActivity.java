package com.example.sit305_101d;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sit305_101d.databinding.ActivityAssessmentBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssessmentActivity extends AppCompatActivity {
    private ActivityAssessmentBinding binding;
    private Question currentQuestion;
    private int score = 0;

    private final String BASE_URL = "http://192.168.0.210:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAssessmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String userInterests = "Technology";

        try (DatabaseHelper db = new DatabaseHelper(this)) {
            String currentUserName = getCurrentUserName();
            User user = db.getUser(currentUserName);
            if (user != null && user.getInterests() != null) {
                userInterests = user.getInterests();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set up the question
        currentQuestion = new Question("What does APK stand for?",
                new String[]{"Android Package Kit", "Apple Program Key", "Android Process Kernel", "All Purpose Kit"}, 0, "Android");

        binding.questionText.setText(currentQuestion.getQuestionText());

        QuizService service = RetrofitClient.getClient(BASE_URL).create(QuizService.class);

        // 1. Hint Generation via Python Bridge
        final String finalUserInterests = userInterests;
        binding.btnGetHint.setOnClickListener(v -> {
            binding.llmHintText.setVisibility(View.VISIBLE);

            binding.llmHintText.setText(R.string.asking_tutor);

            QuizService.ExplanationRequest hintReq = new QuizService.ExplanationRequest(0, 0,
                    "Give a subtle hint for: " + currentQuestion.getQuestionText() + " based on " + finalUserInterests);

            service.explainResults(hintReq).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<QuizService.ExplanationResponse> call, @NonNull Response<QuizService.ExplanationResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        binding.llmHintText.setText(response.body().getExplanation());
                    } else {
                        binding.llmHintText.setText(R.string.tutor_busy);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<QuizService.ExplanationResponse> call, @NonNull Throwable t) {
                    binding.llmHintText.setText(R.string.connection_failed);
                }
            });
        });

        // 2. Submit Logic
        binding.btnSubmit.setOnClickListener(v -> {
            int selectedId = binding.radioGroupOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, R.string.error_select_option, Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isCorrect = (selectedId == binding.radioOption1.getId());
            if (isCorrect) score++;

            navigateToResults();
        });
    }

    private String getCurrentUserName() {
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return preferences.getString("current_user", "");
    }

    private void navigateToResults() {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("SCORE", score);
        intent.putExtra("TOTAL", 1);
        intent.putExtra("TOPIC", currentQuestion.getTopic());
        startActivity(intent);
        finish();
    }
}