package com.example.schedulemanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.schedulemanager.R;
import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.Schedule;
import com.example.schedulemanager.utils.DateTimeUtils;
import com.example.schedulemanager.utils.IntentKeys;
import com.example.schedulemanager.utils.ReminderScheduler;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

    private TextView tvPriority, tvTitle, tvDateTime, tvReminder, tvDescription, tvCategory, tvRecurrence;
    private Button btnToggleComplete;
    private Schedule schedule;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Log.d(TAG, "onCreate");

        dbHelper = DatabaseHelper.getInstance(this);
        schedule = getIntent().getParcelableExtra(IntentKeys.EXTRA_SCHEDULE);

        if (schedule == null) {
            finish();
            return;
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationOnClickListener(v -> finish());

        tvPriority = findViewById(R.id.tvDetailPriority);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvDateTime = findViewById(R.id.tvDetailDateTime);
        tvReminder = findViewById(R.id.tvDetailReminder);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvRecurrence = findViewById(R.id.tvDetailRecurrence);
        btnToggleComplete = findViewById(R.id.btnToggleComplete);

        displaySchedule();

        btnToggleComplete.setOnClickListener(v -> {
            schedule.setCompleted(!schedule.isCompleted());
            dbHelper.updateSchedule(schedule);
            displaySchedule();
        });
    }

    private void displaySchedule() {
        tvTitle.setText(schedule.getTitle());
        tvPriority.setText(schedule.getPriority().getLabel());
        tvPriority.setBackgroundTintList(ContextCompat.getColorStateList(this, schedule.getPriority().getColorResId()));

        if (schedule.getCategory() != null) {
            tvCategory.setText(schedule.getCategory().getLabel());
            tvCategory.setVisibility(android.view.View.VISIBLE);
        } else {
            tvCategory.setVisibility(android.view.View.GONE);
        }

        if (schedule.getRecurrence() != null && schedule.getRecurrence() != com.example.schedulemanager.models.Recurrence.NONE) {
            tvRecurrence.setText(getString(R.string.repeats_label, schedule.getRecurrence().getLabel()));
            tvRecurrence.setVisibility(android.view.View.VISIBLE);
        } else {
            tvRecurrence.setVisibility(android.view.View.GONE);
        }
        
        String dateTime = DateTimeUtils.formatDisplayDate(schedule.getDate()) + " | " + 
                         schedule.getStartTime() + " - " + schedule.getEndTime();
        tvDateTime.setText(dateTime);
        
        tvReminder.setText(schedule.getReminderMinutes() > 0 ? 
                getString(R.string.reminder_info_label) + ": " + schedule.getReminderMinutes() + " minutes before" : getString(R.string.never));
        
        tvDescription.setText(schedule.getDescription().isEmpty() ? 
                getString(R.string.no_description) : schedule.getDescription());

        btnToggleComplete.setText(schedule.isCompleted() ? getString(R.string.mark_incomplete) : getString(R.string.mark_completed));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, AddEditScheduleActivity.class);
            intent.putExtra(IntentKeys.EXTRA_SCHEDULE, schedule);
            startActivity(intent);
            finish(); // Finish detail and reopen from main if needed, or refresh here
            return true;
        } else if (id == R.id.action_delete) {
            showDeleteConfirmation();
            return true;
        } else if (id == R.id.action_share) {
            shareSchedule();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    dbHelper.deleteSchedule(schedule.getId());
                    ReminderScheduler.cancelReminder(this, schedule.getId());
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void shareSchedule() {
        String shareText = "Schedule: " + schedule.getTitle() + "\n" +
                "Date: " + schedule.getDate() + "\n" +
                "Time: " + schedule.getStartTime() + " - " + schedule.getEndTime() + "\n" +
                "Description: " + schedule.getDescription();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }
}
