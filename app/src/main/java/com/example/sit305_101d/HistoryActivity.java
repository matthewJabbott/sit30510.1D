package com.example.sit305_101d;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sit305_101d.databinding.ActivityHistoryBinding;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private ActivityHistoryBinding binding;
    private DatabaseHelper db;
    private String username;
    private List<QuizHistory> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new DatabaseHelper(this);
        username = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("current_user", "");

        setupToolbar();
        loadHistory();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarHistory);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Learning History");
        }
        binding.toolbarHistory.setNavigationOnClickListener(v -> finish());
    }

    private void loadHistory() {
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        historyList = db.getQuizHistory(username);
        if (historyList == null || historyList.isEmpty()) {
            binding.tvEmptyHistory.setVisibility(View.VISIBLE);
            binding.rvHistory.setVisibility(View.GONE);
        } else {
            binding.tvEmptyHistory.setVisibility(View.GONE);
            binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
            binding.rvHistory.setAdapter(new HistoryAdapter(historyList));
        }
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
        private final List<QuizHistory> list;

        public HistoryAdapter(List<QuizHistory> list) { this.list = list; }

        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new HistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
            QuizHistory item = list.get(position);
            TextView tv = holder.itemView.findViewById(android.R.id.text1);
            tv.setText(String.format("%s\nScore: %d/%d\nDate: %s",
                    item.getTopic(), item.getScore(), item.getTotal(), item.getTimestamp()));
            tv.setTextSize(14);
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class HistoryViewHolder extends RecyclerView.ViewHolder {
            public HistoryViewHolder(@NonNull View itemView) { super(itemView); }
        }
    }
}