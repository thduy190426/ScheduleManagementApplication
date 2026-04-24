package com.example.schedulemanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemanager.R;
import com.example.schedulemanager.adapters.ScheduleAdapter;
import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.Schedule;
import com.example.schedulemanager.utils.IntentKeys;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CalendarActivity extends BaseActivity implements ScheduleAdapter.OnScheduleClickListener {

    private static final String TAG = "CalendarActivity";

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ScheduleAdapter adapter;
    private DatabaseHelper dbHelper;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Log.d(TAG, "onCreate");

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Handle Status Bar overlap
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, windowInsets) -> {
            var insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        dbHelper = DatabaseHelper.getInstance(this);

        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new ScheduleAdapter(this);
        recyclerView.setAdapter(adapter);

        setupSwipeToDelete();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            loadSchedulesForDate(selectedDate);
        });

        // Load today by default
        Calendar calendar = Calendar.getInstance();
        selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
        loadSchedulesForDate(selectedDate);
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Schedule schedule = adapter.getCurrentList().get(position);

                dbHelper.deleteSchedule(schedule.getId());
                loadSchedulesForDate(selectedDate);

                Snackbar.make(recyclerView, R.string.schedule_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            dbHelper.addSchedule(schedule);
                            loadSchedulesForDate(selectedDate);
                        }).show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void loadSchedulesForDate(String date) {
        List<Schedule> schedules = dbHelper.getSchedulesByDate(date);
        adapter.submitList(schedules);
        tvEmpty.setVisibility(schedules.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onScheduleClick(Schedule schedule) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(IntentKeys.EXTRA_SCHEDULE, schedule);
        startActivity(intent);
    }

    @Override
    public void onScheduleCheckChanged(Schedule schedule, boolean isChecked) {
        new Thread(() -> {
            dbHelper.updateSchedule(schedule);
        }).start();
    }
}
