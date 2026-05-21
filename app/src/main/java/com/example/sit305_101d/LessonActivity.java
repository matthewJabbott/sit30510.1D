package com.example.sit305_101d;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sit305_101d.databinding.ActivityLessonBinding;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LessonActivity extends AppCompatActivity {
    private ActivityLessonBinding binding;
    private List<Question> questionList;
    private int currentIndex = 0;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLessonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String topic = getIntent().getStringExtra("SELECTED_TASK");
        fetchQuiz(topic != null ? topic : getString(R.string.default_topic));

        binding.btnSubmit.setOnClickListener(v -> handleSubmission());
    }

    private void fetchQuiz(String topic) {
        String cleanTopic = topic.replace("Selected:", "").replace("Selected", "").trim();

        binding.lessonProgress.setVisibility(View.VISIBLE);
        binding.tvLessonTitle.setText(R.string.ai_generating);
        binding.btnSubmit.setEnabled(false);

        String baseUrl = "http://10.0.2.2:8080/";

        QuizService service = RetrofitClient.getClient(baseUrl).create(QuizService.class);

        // FIX: Replaced explicit type with diamond operator <>
        service.getQuiz(cleanTopic).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<QuizService.QuizResponse> call, @NonNull Response<QuizService.QuizResponse> response) {
                binding.lessonProgress.setVisibility(View.GONE);
                binding.btnSubmit.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    questionList = response.body().getQuestions();
                    if (questionList != null && !questionList.isEmpty()) {
                        displayQuestion();
                    } else {
                        binding.tvQuestion.setText(R.string.error_no_questions);
                    }
                } else {
                    binding.tvQuestion.setText(getString(R.string.server_error, response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<QuizService.QuizResponse> call, @NonNull Throwable t) {
                binding.lessonProgress.setVisibility(View.GONE);
                binding.tvLessonTitle.setText(R.string.connection_failed_title);
                binding.tvQuestion.setText(getString(R.string.error_generic, t.getMessage()));
            }
        });
    }

    private void displayQuestion() {
        if (questionList == null || currentIndex >= questionList.size()) return;

        Question q = questionList.get(currentIndex);
        binding.tvQuestion.setText(q.getQuestionText());

        String[] options = q.getOptions();
        if (options.length >= 4) {
            binding.rbOption1.setText(options[0]);
            binding.rbOption2.setText(options[1]);
            binding.rbOption3.setText(options[2]);
            binding.rbOption4.setText(options[3]);
        }
        binding.rgAnswers.clearCheck();
    }

    private void handleSubmission() {
        int selectedId = binding.rgAnswers.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, R.string.select_answer_prompt, Toast.LENGTH_SHORT).show();
            return;
        }

        View radioButton = binding.rgAnswers.findViewById(selectedId);
        int selectedIdx = binding.rgAnswers.indexOfChild(radioButton);

        if (selectedIdx == questionList.get(currentIndex).getCorrectAnswerIndex()) {
            score++;
        }

        currentIndex++;
        if (currentIndex < questionList.size()) {
            displayQuestion();
        } else {
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("SCORE", score);
            intent.putExtra("TOTAL", questionList.size());
            startActivity(intent);
            finish();
        }
    }
}