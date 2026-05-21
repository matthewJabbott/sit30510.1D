package com.example.sit305_101d;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Body;


public interface QuizService {
    @GET("getQuiz")
    Call<QuizResponse> getQuiz(@Query("topic") String topic);

    class QuizResponse {
        @SerializedName("quiz") // Matches the key in your Flask output
        private List<Question> quiz;

        public List<Question> getQuestions() {
            return quiz;
        }
    }

    @POST("explainResults")
    Call<ExplanationResponse> explainResults(@Body ExplanationRequest request);

    class ExplanationRequest {
        int score;
        int total;
        String topic;
        public ExplanationRequest(int score, int total, String topic) {
            this.score = score;
            this.total = total;
            this.topic = topic;
        }
    }
    @POST("createPaymentIntent")
    Call<com.google.gson.JsonObject> createPaymentIntent(@Body PaymentIntentRequest request);

    static class PaymentIntentRequest {
        int amount;
        String currency;
        String tier;
        String username;

        public PaymentIntentRequest(int amount, String currency, String tier, String username) {
            this.amount = amount;
            this.currency = currency;
            this.tier = tier;
            this.username = username;
        }
    }
    class ExplanationResponse {
        private String explanation;
        public String getExplanation() { return explanation; }
    }
}