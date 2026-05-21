package com.example.sit305_101d;

public class User {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String interests;
    private String level;

    public User(String name, String email, String password, String phone, String interests, String level) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.interests = interests;
        this.level = level;
    }

    public String getName() { return name; }
    public String getInterests() { return interests; }
    public String getLearningLevel() { return level; }
}