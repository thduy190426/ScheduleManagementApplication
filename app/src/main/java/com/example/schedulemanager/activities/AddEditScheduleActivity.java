package com.example.schedulemanager.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.schedulemanager.R;
import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.Priority;
import com.example.schedulemanager.models.Schedule;
import com.example.schedulemanager.utils.DateTimeUtils;
import com.example.schedulemanager.utils.IntentKeys;
import com.example.schedulemanager.utils.PreferenceManager;
import com.example.schedulemanager.utils.ReminderScheduler;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;
import java.util.Locale;

/**
 * Activity for adding or editing a schedule item.
 */
public class AddEditScheduleActivity extends AppCompatActivity {

    private static final String TAG = "AddEditScheduleActivity";

    private TextInputEditText etTitle, etDescription;
    private Button btnPickDate, btnStartTime, btnEndTime, btnSave;
    private Spinner spinnerPriority, spinnerReminder, spinnerCategory, spinnerRecurrence;

    private String selectedDate, selectedStartTime, selectedEndTime;
    private Schedule existingSchedule;
    private boolean isEditMode = false;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_schedule);
        Log.d(TAG, "onCreate");

        dbHelper = DatabaseHelper.getInstance(this);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnStartTime = findViewById(R.id.btnStartTime);
        btnEndTime = findViewById(R.id.btnEndTime);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerReminder = findViewById(R.id.spinnerReminder);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerRecurrence = findViewById(R.id.spinnerRecurrence);
        btnSave = findViewById(R.id.btnSave);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? R.string.edit_schedule : R.string.add_schedule);
        }

        // Xử lý đè Status Bar bằng WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, windowInsets) -> {
            var insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        setupSpinners();
        setupPickers();

        if (getIntent().hasExtra(IntentKeys.EXTRA_SCHEDULE)) {
            existingSchedule = getIntent().getParcelableExtra(IntentKeys.EXTRA_SCHEDULE);
            isEditMode = true;
            populateFields();
        } else {
            selectedDate = DateTimeUtils.getCurrentDate();
            selectedStartTime = "08:00";
            selectedEndTime = "09:00";
            spinnerReminder.setSelection(getReminderIndex(new PreferenceManager(this).getDefaultReminder()));
            updateDisplayValues();
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveSchedule());
    }

    private void setupSpinners() {
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{Priority.LOW.getLabel(), Priority.MEDIUM.getLabel(), Priority.HIGH.getLabel()});
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        getString(R.string.reminder_none),
                        getString(R.string.reminder_5min),
                        getString(R.string.reminder_10min),
                        getString(R.string.reminder_15min),
                        getString(R.string.reminder_30min),
                        getString(R.string.reminder_60min)
                });
        reminderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminder.setAdapter(reminderAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        com.example.schedulemanager.models.Category.WORK.getLabel(),
                        com.example.schedulemanager.models.Category.PERSONAL.getLabel(),
                        com.example.schedulemanager.models.Category.STUDY.getLabel(),
                        com.example.schedulemanager.models.Category.HEALTH.getLabel(),
                        com.example.schedulemanager.models.Category.OTHER.getLabel()
                });
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> recurrenceAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        com.example.schedulemanager.models.Recurrence.NONE.getLabel(),
                        com.example.schedulemanager.models.Recurrence.DAILY.getLabel(),
                        com.example.schedulemanager.models.Recurrence.WEEKLY.getLabel(),
                        com.example.schedulemanager.models.Recurrence.MONTHLY.getLabel(),
                        com.example.schedulemanager.models.Recurrence.YEARLY.getLabel()
                });
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecurrence.setAdapter(recurrenceAdapter);
    }

    private void setupPickers() {
        btnPickDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            if (isEditMode || selectedDate != null) {
                c = DateTimeUtils.getCalendar(selectedDate, "00:00");
            }
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                btnPickDate.setText(DateTimeUtils.formatDisplayDate(selectedDate));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnStartTime.setOnClickListener(v -> {
            String[] parts = selectedStartTime.split(":");
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedStartTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                btnStartTime.setText(selectedStartTime);
            }, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), true).show();
        });

        btnEndTime.setOnClickListener(v -> {
            String[] parts = selectedEndTime.split(":");
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedEndTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                btnEndTime.setText(selectedEndTime);
            }, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), true).show();
        });
    }

    private void populateFields() {
        etTitle.setText(existingSchedule.getTitle());
        etDescription.setText(existingSchedule.getDescription());
        selectedDate = existingSchedule.getDate();
        selectedStartTime = existingSchedule.getStartTime();
        selectedEndTime = existingSchedule.getEndTime();
        spinnerPriority.setSelection(existingSchedule.getPriority().toInt());
        spinnerCategory.setSelection(existingSchedule.getCategory() != null ? existingSchedule.getCategory().toInt() : 0);
        spinnerRecurrence.setSelection(existingSchedule.getRecurrence() != null ? existingSchedule.getRecurrence().toInt() : 0);
        
        int reminderIndex = getReminderIndex(existingSchedule.getReminderMinutes());
        spinnerReminder.setSelection(reminderIndex);
        
        updateDisplayValues();
    }

    private void updateDisplayValues() {
        btnPickDate.setText(DateTimeUtils.formatDisplayDate(selectedDate));
        btnStartTime.setText(selectedStartTime);
        btnEndTime.setText(selectedEndTime);
    }

    private int getReminderIndex(int minutes) {
        switch (minutes) {
            case 5: return 1;
            case 10: return 2;
            case 15: return 3;
            case 30: return 4;
            case 60: return 5;
            default: return 0;
        }
    }

    private int getReminderMinutes(int index) {
        switch (index) {
            case 1: return 5;
            case 2: return 10;
            case 3: return 15;
            case 4: return 30;
            case 5: return 60;
            default: return 0;
        }
    }

    private void saveSchedule() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            etTitle.setError(getString(R.string.name_required));
            return;
        }

        if (!DateTimeUtils.isTimeBefore(selectedStartTime, selectedEndTime)) {
            Snackbar.make(btnSave, R.string.error_invalid_time, Snackbar.LENGTH_LONG).show();
            return;
        }

        if (DateTimeUtils.isDateInPast(selectedDate, selectedStartTime)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.past_date_warning)
                    .setPositiveButton(R.string.proceed, (dialog, which) -> performSave(title))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            performSave(title);
        }
    }

    private void performSave(String title) {
        Schedule schedule = isEditMode ? existingSchedule : new Schedule();
        schedule.setTitle(title);
        schedule.setDescription(etDescription.getText().toString().trim());
        schedule.setDate(selectedDate);
        schedule.setStartTime(selectedStartTime);
        schedule.setEndTime(selectedEndTime);
        schedule.setPriority(com.example.schedulemanager.models.Priority.fromInt(spinnerPriority.getSelectedItemPosition()));
        schedule.setCategory(com.example.schedulemanager.models.Category.fromInt(spinnerCategory.getSelectedItemPosition()));
        schedule.setRecurrence(com.example.schedulemanager.models.Recurrence.fromInt(spinnerRecurrence.getSelectedItemPosition()));
        schedule.setReminderMinutes(getReminderMinutes(spinnerReminder.getSelectedItemPosition()));
        if (!isEditMode) {
            schedule.setCreatedAt(DateTimeUtils.getCurrentDateTime());
            schedule.setCompleted(false);
        }

        long result;
        if (isEditMode) {
            result = dbHelper.updateSchedule(schedule);
        } else {
            result = dbHelper.insertSchedule(schedule);
            schedule.setId((int) result);
        }

        if (result != -1) {
            ReminderScheduler.scheduleReminder(this, schedule);
            setResult(RESULT_OK);
            finish();
        } else {
            Snackbar.make(btnSave, R.string.error_saving_schedule, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", etTitle.getText().toString());
        outState.putString("date", selectedDate);
        outState.putString("start", selectedStartTime);
        outState.putString("end", selectedEndTime);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        etTitle.setText(savedInstanceState.getString("title"));
        selectedDate = savedInstanceState.getString("date");
        selectedStartTime = savedInstanceState.getString("start");
        selectedEndTime = savedInstanceState.getString("end");
        updateDisplayValues();
    }
}
