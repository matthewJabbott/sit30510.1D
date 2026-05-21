package com.example.sit305_101d;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "LearningAssistant.db";
    private static final int DATABASE_VERSION = 4;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE user_profile (" +
                "id INTEGER PRIMARY KEY, " +
                "name TEXT UNIQUE, " +
                "email TEXT, " +
                "password TEXT, " +
                "phone TEXT, " +
                "interests TEXT, " +
                "level TEXT, " +
                "tier TEXT DEFAULT 'None', " +
                "is_premium INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE quiz_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_name TEXT, " +
                "topic TEXT, " +
                "score INTEGER, " +
                "total INTEGER, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user_profile");
        db.execSQL("DROP TABLE IF EXISTS quiz_history");
        onCreate(db);
    }

    public User getUser(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("user_profile", null, "name=?", new String[]{name}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                    cursor.getString(cursor.getColumnIndexOrThrow("interests")),
                    cursor.getString(cursor.getColumnIndexOrThrow("level"))
            );
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public boolean checkUser(String name, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("user_profile", new String[]{"id"},
                "name=? AND password=?", new String[]{name, password}, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public long registerUser(String name, String email, String password, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);
        values.put("phone", phone);
        values.put("interests", "");
        values.put("level", "Beginner");
        values.put("is_premium", 0);
        return db.insertWithOnConflict("user_profile", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void updateUserProfile(String name, String interests, String level) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("interests", interests);
        values.put("level", level);
        db.update("user_profile", values, "name = ?", new String[]{name});
    }

    public void insertQuizHistory(String userName, String topic, int score, int total) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_name", userName);
        values.put("topic", topic);
        values.put("score", score);
        values.put("total", total);
        db.insert("quiz_history", null, values);
    }

    public List<QuizHistory> getQuizHistory(String userName) {
        List<QuizHistory> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("quiz_history", null, "user_name=?", new String[]{userName},
                null, null, "timestamp DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                QuizHistory h = new QuizHistory(
                        cursor.getString(cursor.getColumnIndexOrThrow("topic")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("score")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("total")),
                        cursor.getString(cursor.getColumnIndexOrThrow("timestamp"))
                );
                history.add(h);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return history;
    }

    public void updatePremiumStatus(String name, boolean isPremium) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_premium", isPremium ? 1 : 0);
        db.update("user_profile", values, "name = ?", new String[]{name});
    }
    public String getTier(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("user_profile", new String[]{"tier"}, "name=?", new String[]{name}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String tier = cursor.getString(0);
            cursor.close();
            return tier != null ? tier : "None";
        }
        return "None";
    }

    public void updateTier(String name, String tier) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tier", tier);
        db.update("user_profile", values, "name = ?", new String[]{name});
    }
}