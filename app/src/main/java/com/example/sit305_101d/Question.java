package com.example.sit305_101d;

import com.google.gson.annotations.SerializedName;

public class Question {
    @SerializedName("questionText")
    private String questionText;

    @SerializedName("options")
    private String[] options;

    @SerializedName("correctAnswerIndex")
    private int correctAnswerIndex;

    private String topic;

    // 1. Empty Constructor: Required for Retrofit/JSON to work
    public Question() {}

    // 2. Full Constructor: Required for your hardcoded AssessmentActivity questions
    public Question(String questionText, String[] options, int correctAnswerIndex, String topic) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.topic = topic;
    }

    // Getters
    public String getQuestionText() { return questionText; }
    public String[] getOptions() { return options; }
    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public String getTopic() { return topic; }
}