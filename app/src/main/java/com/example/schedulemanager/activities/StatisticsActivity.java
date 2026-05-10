package com.example.schedulemanager.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.schedulemanager.R;
import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.Category;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class StatisticsActivity extends BaseActivity {

    private ProgressBar progressBar;
    private TextView tvPercentage, tvCount;
    private DatabaseHelper dbHelper;
    private com.github.mikephil.charting.charts.BarChart barChart;
    private com.github.mikephil.charting.charts.PieChart pieChart;

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
            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setTint(ContextCompat.getColor(this, R.color.white));
            }
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progressBar);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvCount = findViewById(R.id.tvCount);
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);

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

        setupPieChart();

        setupBarChart();
    }

    private void setupPieChart() {
        Map<String, Integer> catStats = dbHelper.getCategoryStatistics();
        if (catStats.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            return;
        }
        pieChart.setVisibility(View.VISIBLE);

        ArrayList<com.github.mikephil.charting.data.PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : catStats.entrySet()) {
            String label = entry.getKey();
            try {
                Category cat = Category.valueOf(label);
                label = getString(cat.getLabelResId());
            } catch (IllegalArgumentException ignored) {}
            
            entries.add(new com.github.mikephil.charting.data.PieEntry(entry.getValue(), label));
        }

        com.github.mikephil.charting.data.PieDataSet dataSet = new com.github.mikephil.charting.data.PieDataSet(entries, "");
        
        int[] colors = {
            Color.parseColor("#4285F4"), Color.parseColor("#EA4335"),
            Color.parseColor("#FBBC05"), Color.parseColor("#34A853"),
            Color.parseColor("#8E24AA"), Color.parseColor("#00ACC1"),
            Color.parseColor("#F4511E"), Color.parseColor("#795548")
        };
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(3f);

        com.github.mikephil.charting.data.PieData pieData = new com.github.mikephil.charting.data.PieData(dataSet);
        pieChart.setData(pieData);

        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(11f);
        pieChart.setDrawEntryLabels(false);

        com.github.mikephil.charting.components.Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);

        pieChart.animateXY(1000, 1000);
        pieChart.invalidate();
    }

    private void setupBarChart() {
        Map<String, Integer> trendStats = dbHelper.getWeeklyTrends();
        if (trendStats.isEmpty()) {
            barChart.setVisibility(View.GONE);
            return;
        }
        barChart.setVisibility(View.VISIBLE);

        java.util.ArrayList<com.github.mikephil.charting.data.BarEntry> entries = new java.util.ArrayList<>();
        final java.util.ArrayList<String> labels = new java.util.ArrayList<>();
        
        int index = 0;
        for (Map.Entry<String, Integer> entry : trendStats.entrySet()) {
            entries.add(new com.github.mikephil.charting.data.BarEntry(index, entry.getValue()));
            labels.add(entry.getKey().substring(5));
            index++;
        }

        com.github.mikephil.charting.data.BarDataSet dataSet = new com.github.mikephil.charting.data.BarDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(this, R.color.primary));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        com.github.mikephil.charting.data.BarData barData = new com.github.mikephil.charting.data.BarData(dataSet);
        barChart.setData(barData);

        // Customizing the chart
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);

        com.github.mikephil.charting.components.XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels));

        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);

        barChart.animateY(1000);
        barChart.invalidate();
    }
}
