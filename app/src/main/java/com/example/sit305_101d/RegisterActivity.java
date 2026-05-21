package com.example.sit305_101d;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sit305_101d.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        binding.btnRegisterSubmit.setOnClickListener(v -> {
            String user = binding.etRegUser.getText().toString().trim();
            String email = binding.etRegEmail.getText().toString().trim();
            String emailVerify = binding.etRegEmailVerify.getText().toString().trim();
            String pass = binding.etRegPass.getText().toString().trim();
            String passVerify = binding.etRegPassVerify.getText().toString().trim();
            String phone = binding.etRegPhone.getText().toString().trim();

            // Validation Logic
            if (user.isEmpty() || email.isEmpty() || pass.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.equals(emailVerify)) {
                Toast.makeText(this, "Emails do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(passVerify)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to DB via the helper method
            long id = dbHelper.registerUser(user, email, pass, phone);

            if (id != -1) {
                Toast.makeText(this, "Details Saved! Now pick your interests.", Toast.LENGTH_SHORT).show();

                // 1. Create the link (Intent) to the next page
                Intent intent = new Intent(RegisterActivity.this, InterestsActivity.class);

                // 2. Pass the username so the next page knows whose profile to update
                intent.putExtra("USERNAME_KEY", user);

                startActivity(intent);

            }
            else {
                Toast.makeText(this, "Error: Username might already exist", Toast.LENGTH_SHORT).show();
            }
        });
    }
}