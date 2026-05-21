package com.example.sit305_101d;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sit305_101d.databinding.ActivityShareBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.HashMap;
import java.util.Map;

public class ShareActivity extends AppCompatActivity {
    private ActivityShareBinding binding;
    private DatabaseHelper db;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new DatabaseHelper(this);
        username = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("current_user", "");
        User user = db.getUser(username);

        setupToolbar();
        displayProfile(user);
        setupShareButton();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarShare);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Share Profile");
        }
        binding.toolbarShare.setNavigationOnClickListener(v -> finish());
    }

    private void displayProfile(User user) {
        if (user == null) return;
        binding.tvShareName.setText(user.getName());
        binding.tvShareInterests.setText("Interests: " + (user.getInterests().isEmpty() ? "General IT" : user.getInterests()));
        binding.tvShareLevel.setText("Level: " + user.getLearningLevel());

        String shareData = String.format("https://learningassistant.app/profile?user=%s&interests=%s&level=%s",
                user.getName(), user.getInterests(), user.getLearningLevel());
        generateQRCode(shareData);
        binding.tvShareUrl.setText(shareData);
    }

    private void generateQRCode(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, String> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            binding.ivQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            binding.ivQrCode.setImageResource(android.R.drawable.ic_menu_gallery);
            Toast.makeText(this, "Failed to generate QR", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupShareButton() {
        binding.btnShare.setOnClickListener(v -> {
            String shareText = binding.tvShareUrl.getText().toString();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Share Profile via"));
        });
    }
}