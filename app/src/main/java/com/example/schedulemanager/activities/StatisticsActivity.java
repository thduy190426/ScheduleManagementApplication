package com.example.schedulemanager.activities;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.schedulemanager.R;
import com.example.schedulemanager.database.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvPercentage, tvCount;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        dbHelper = DatabaseHelper.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progressBar);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvCount = findViewById(R.id.tvCount);

        loadStatistics();
    }

    private void loadStatistics() {
        int total = dbHelper.getTotalSchedulesCount();
        int completed = dbHelper.getCompletedSchedulesCount();

        if (total > 0) {
            int percentage = (completed * 100) / total;
            progressBar.setProgress(percentage);
            tvPercentage.setText(String.format(Locale.getDefault(), "%d%%", percentage));
            tvCount.setText(getString(R.string.completed_vs_total, completed, total));
        } else {
            progressBar.setProgress(0);
            tvPercentage.setText("0%");
            tvCount.setText(getString(R.string.no_data));
        }
    }
}
