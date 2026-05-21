package com.example.sit305_101d;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sit305_101d.databinding.ActivityPurchaseBinding;
import com.google.gson.JsonObject;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PurchaseActivity extends AppCompatActivity {
    private static final String TAG = "STRIPE_DEBUG";
    private ActivityPurchaseBinding binding;
    private DatabaseHelper db;
    private String username;
    private String selectedTier = "";

    private PaymentSheet paymentSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPurchaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Stripe Configuration
        PaymentConfiguration.init(
                this,
                "key"
        );

        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        db = new DatabaseHelper(this);
        username = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("current_user", "");

        setupToolbar();
        setupTierSelection();
        checkExistingTier();
    }

    private void onPaymentResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            db.updateTier(username, selectedTier);
            Toast.makeText(this, "Success! " + selectedTier + " Plan activated.", Toast.LENGTH_LONG).show();
            binding.tvPremiumStatus.setText("Current Plan: " + selectedTier);
            binding.btnPurchase.setText("Subscribed");
            binding.btnPurchase.setEnabled(false);
            finish();
        } else if (result instanceof PaymentSheetResult.Failed) {
            Throwable error = ((PaymentSheetResult.Failed) result).getError();
            Log.e(TAG, "Payment failed", error);
            Toast.makeText(this, "Payment failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            binding.btnPurchase.setEnabled(true);
            binding.btnPurchase.setText("Try Again");
        } else {
            Log.d(TAG, "Payment cancelled by user");
            binding.btnPurchase.setEnabled(true);
            binding.btnPurchase.setText("Purchase " + selectedTier);
        }
    }

    private void callBackendForPayment() {
        binding.btnPurchase.setEnabled(false);
        binding.btnPurchase.setText("Connecting to Secure Gateway...");

        // Prepare request object
        int amountCents = selectedTier.equals("Starter") ? 299 : (selectedTier.equals("Advanced") ? 799 : 499);
        QuizService.PaymentIntentRequest req = new QuizService.PaymentIntentRequest(amountCents, "usd", selectedTier, username);

        // Call your Unified Flask Backend
        QuizService service = RetrofitClient.getClient("http://192.168.0.210:8080/").create(QuizService.class);
        service.createPaymentIntent(req).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String clientSecret = response.body().get("clientSecret").getAsString();
                        Log.d(TAG, "Secret received: " + clientSecret);

                        // Launch Stripe UI on the Main Thread
                        runOnUiThread(() -> {
                            final PaymentSheet.Configuration config = new PaymentSheet.Configuration.Builder("GlassBox AI")
                                    .allowsDelayedPaymentMethods(true)
                                    .build();
                            paymentSheet.presentWithPaymentIntent(clientSecret, config);
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "JSON Parsing error", e);
                        handleError("Data processing error");
                    }
                } else {
                    Log.e(TAG, "Server error code: " + response.code());
                    handleError("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Log.e(TAG, "Network failure", t);
                handleError("Network error: " + t.getMessage());
            }
        });
    }

    private void handleError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(PurchaseActivity.this, message, Toast.LENGTH_LONG).show();
            binding.btnPurchase.setEnabled(true);
            binding.btnPurchase.setText("Try Again");
        });
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarPurchase);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Premium Plans");
        }
        binding.toolbarPurchase.setNavigationOnClickListener(v -> finish());
    }

    private void setupTierSelection() {
        binding.btnTierStarter.setOnClickListener(v -> selectTier("Starter", binding.btnTierStarter));
        binding.btnTierIntermediate.setOnClickListener(v -> selectTier("Intermediate", binding.btnTierIntermediate));
        binding.btnTierAdvanced.setOnClickListener(v -> selectTier("Advanced", binding.btnTierAdvanced));

        binding.btnPurchase.setOnClickListener(v -> {
            if (selectedTier.isEmpty()) {
                Toast.makeText(this, "Please select a plan first", Toast.LENGTH_SHORT).show();
                return;
            }
            initiatePayment();
        });
    }

    private void selectTier(String tier, Button selectedBtn) {
        selectedTier = tier;

        // Reset colors
        binding.btnTierStarter.setBackgroundTintList(ColorStateList.valueOf(0xFFA5D6A7));
        binding.btnTierIntermediate.setBackgroundTintList(ColorStateList.valueOf(0xFF64B5F6));
        binding.btnTierAdvanced.setBackgroundTintList(ColorStateList.valueOf(0xFFCE93D8));

        // Highlight selection
        selectedBtn.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        selectedBtn.setTextColor(Color.WHITE);

        binding.btnPurchase.setEnabled(true);
        String price = tier.equals("Starter") ? "$2.99" : (tier.equals("Advanced") ? "$7.99" : "$4.99");
        binding.btnPurchase.setText("Purchase " + tier + " (" + price + "/mo)");
    }

    private void checkExistingTier() {
        String existingTier = db.getTier(username);
        if (existingTier != null && !existingTier.equals("None")) {
            binding.tvPremiumStatus.setText("Current Plan: " + existingTier);
            binding.btnPurchase.setText("Already Subscribed");
            binding.btnPurchase.setEnabled(false);
        }
    }

    private void initiatePayment() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Subscription")
                .setMessage("Subscribe to " + selectedTier + " Plan?")
                .setPositiveButton("Pay Now", (dialog, which) -> callBackendForPayment())
                .setNegativeButton("Cancel", null)
                .show();
    }
}