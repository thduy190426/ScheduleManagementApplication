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

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.ActivityOptionsCompat;

public class CalendarActivity extends BaseActivity implements ScheduleAdapter.OnScheduleClickListener {

    private static final String TAG = "CalendarActivity";

    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ScheduleAdapter adapter;
    private DatabaseHelper dbHelper;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setTint(getResources().getColor(R.color.white));
            }
        }

        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("OPEN_DRAWER", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

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

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                    date.getYear(), date.getMonth(), date.getDay());
            loadSchedulesForDate(selectedDate);
        });

        Calendar calendar = Calendar.getInstance();
        selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
        
        calendarView.setSelectedDate(LocalDate.now());
        loadSchedulesForDate(selectedDate);
        highlightDatesWithSchedules();
    }

    private void highlightDatesWithSchedules() {
        calendarView.removeDecorators();
        
        List<String> dateStrings = dbHelper.getDatesWithSchedules();
        List<CalendarDay> calendarDays = new ArrayList<>();
        for (String dateStr : dateStrings) {
            try {
                String[] parts = dateStr.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                calendarDays.add(CalendarDay.from(year, month, day));
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date: " + dateStr, e);
            }
        }
        calendarView.addDecorator(new EventDecorator(getResources().getColor(R.color.primary), calendarDays));
    }

    private class EventDecorator implements DayViewDecorator {
        private final int color;
        private final HashSet<CalendarDay> dates;

        public EventDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(8, color));
        }
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                
                adapter.moveItem(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Schedule schedule = adapter.getCurrentList().get(position);

                dbHelper.deleteSchedule(schedule.getId());
                loadSchedulesForDate(selectedDate);
                highlightDatesWithSchedules();

                Snackbar.make(recyclerView, R.string.schedule_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            dbHelper.addSchedule(schedule);
                            loadSchedulesForDate(selectedDate);
                            highlightDatesWithSchedules();
                        }).show();
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSchedulesForDate(selectedDate);
        highlightDatesWithSchedules();
    }

    private void loadSchedulesForDate(String date) {
        List<Schedule> schedules = dbHelper.getSchedulesByDate(date);
        adapter.submitList(schedules);
        tvEmpty.setVisibility(schedules.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onScheduleClick(Schedule schedule, View view) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(IntentKeys.EXTRA_SCHEDULE, schedule);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, view, view.getTransitionName());
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onScheduleCheckChanged(Schedule schedule, boolean isChecked) {
        new Thread(() -> {
            dbHelper.updateSchedule(schedule);
        }).start();
    }
}
