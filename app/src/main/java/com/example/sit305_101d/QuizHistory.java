package com.example.sit305_101d;

public class QuizHistory {
    private String topic;
    private int score;
    private int total;
    private String timestamp;

    public QuizHistory(String topic, int score, int total, String timestamp) {
        this.topic = topic;
        this.score = score;
        this.total = total;
        this.timestamp = timestamp;
    }

    public String getTopic() { return topic; }
    public int getScore() { return score; }
    public int getTotal() { return total; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("%s | Score: %d/%d | %s", topic, score, total, timestamp);
    }
}