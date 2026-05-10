package com.example.schedulemanager.activities;

import android.content.Intent;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemanager.R;
import com.example.schedulemanager.adapters.IconAdapter;
import com.example.schedulemanager.adapters.SubTaskAdapter;
import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.CustomCategory;
import com.example.schedulemanager.models.Priority;
import com.example.schedulemanager.models.Schedule;
import com.example.schedulemanager.models.SubTask;
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddEditScheduleActivity extends BaseActivity {

    private static final String TAG = "AddEditScheduleActivity";

    private TextInputEditText etTitle, etDescription;
    private Button btnPickDate, btnStartTime, btnEndTime, btnSave, btnAddSubTask, btnAddAttachment;
    private ImageButton btnRemoveAttachment;
    private ImageView ivAttachment;
    private View layoutAttachment;
    private Spinner spinnerPriority, spinnerReminder, spinnerCategory, spinnerRecurrence;
    private RecyclerView rvSubTasks;
    private SubTaskAdapter subTaskAdapter;
    private List<SubTask> subTasks = new java.util.ArrayList<>();

    private String selectedDate, selectedStartTime, selectedEndTime;
    private String customCategoryName;
    private String currentAttachmentPath;
    private Schedule existingSchedule;
    private boolean isEditMode = false;
    private DatabaseHelper dbHelper;

    private final ActivityResultLauncher<String[]> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        
                        currentAttachmentPath = uri.toString();
                        showAttachment(currentAttachmentPath);
                    } catch (SecurityException e) {
                        Log.e(TAG, "Error taking persistable permission", e);
                        Toast.makeText(this, "Could not persist permission", Toast.LENGTH_SHORT).show();
                        currentAttachmentPath = uri.toString();
                        showAttachment(currentAttachmentPath);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_schedule);

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
        rvSubTasks = findViewById(R.id.rvSubTasks);
        btnAddSubTask = findViewById(R.id.btnAddSubTask);
        btnSave = findViewById(R.id.btnSave);
        btnAddAttachment = findViewById(R.id.btnAddAttachment);
        btnRemoveAttachment = findViewById(R.id.btnRemoveAttachment);
        ivAttachment = findViewById(R.id.ivAttachment);
        layoutAttachment = findViewById(R.id.layoutAttachment);

        setupSpinners();
        setupPickers();
        setupCategorySelection();
        setupSubTasksList();
        setupAttachmentLogic();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? R.string.edit_schedule : R.string.add_schedule);
        }

        if (getIntent().hasExtra(IntentKeys.EXTRA_SCHEDULE)) {
            existingSchedule = getIntent().getParcelableExtra(IntentKeys.EXTRA_SCHEDULE);
            isEditMode = true;
            customCategoryName = existingSchedule.getCustomCategory();
            populateFields();
        } else if (getIntent().hasExtra(IntentKeys.EXTRA_QUICK_ADD_TITLE)) {
            String quickTitle = getIntent().getStringExtra(IntentKeys.EXTRA_QUICK_ADD_TITLE);
            long quickTime = getIntent().getLongExtra(IntentKeys.EXTRA_QUICK_ADD_TIME, -1);

            etTitle.setText(quickTitle);
            if (quickTime != -1) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(quickTime);

                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
                selectedStartTime = String.format(Locale.getDefault(), "%02d:%02d",
                        cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));

                cal.add(Calendar.HOUR_OF_DAY, 1);
                selectedEndTime = String.format(Locale.getDefault(), "%02d:%02d",
                        cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            } else {
                selectedDate = DateTimeUtils.getCurrentDate(this);
                selectedStartTime = "08:00";
                selectedEndTime = "09:00";
            }
            spinnerReminder.setSelection(getReminderIndex(new PreferenceManager(this).getDefaultReminder()));
            updateDisplayValues();
        } else {
            selectedDate = DateTimeUtils.getCurrentDate(this);
            selectedStartTime = "08:00";
            selectedEndTime = "09:00";
            spinnerReminder.setSelection(getReminderIndex(new PreferenceManager(this).getDefaultReminder()));
            updateDisplayValues();
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        btnSave.setOnClickListener(v -> saveSchedule());
    }

    private void setupSubTasksList() {
        subTaskAdapter = new SubTaskAdapter(subTasks, position -> {
            subTasks.remove(position);
            subTaskAdapter.notifyItemRemoved(position);
        });
        rvSubTasks.setLayoutManager(new LinearLayoutManager(this));
        rvSubTasks.setAdapter(subTaskAdapter);

        btnAddSubTask.setOnClickListener(v -> {
            subTasks.add(new SubTask());
            subTaskAdapter.notifyItemInserted(subTasks.size() - 1);
        });
    }

    private void setupAttachmentLogic() {
        btnAddAttachment.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));
        btnRemoveAttachment.setOnClickListener(v -> {
            currentAttachmentPath = null;
            layoutAttachment.setVisibility(View.GONE);
            btnAddAttachment.setVisibility(View.VISIBLE);
        });
    }

    private void showAttachment(String path) {
        if (path != null && !path.isEmpty()) {
            try {
                ivAttachment.setImageURI(android.net.Uri.parse(path));
                layoutAttachment.setVisibility(View.VISIBLE);
                btnAddAttachment.setVisibility(View.GONE);
            } catch (Exception e) {
                Log.e(TAG, "Error showing attachment", e);
                layoutAttachment.setVisibility(View.GONE);
                btnAddAttachment.setVisibility(View.VISIBLE);
            }
        } else {
            layoutAttachment.setVisibility(View.GONE);
            btnAddAttachment.setVisibility(View.VISIBLE);
        }
    }

    private void setupCategorySelection() {
        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (com.example.schedulemanager.models.Category.fromInt(position) == com.example.schedulemanager.models.Category.OTHER) {
                    showCustomCategoryDialog();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void showCustomCategoryDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_category, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etCategoryName);
        RadioGroup rgColors = dialogView.findViewById(R.id.rgColors);
        RecyclerView rvIcons = dialogView.findViewById(R.id.rvIcons);

        if (customCategoryName != null) {
            etName.setText(customCategoryName);
        }

        final int[] selectedColor = {0xFF2196F3};
        final String[] selectedIcon = {"ic_category"};

        final List<String> availableIcons = Arrays.asList(
                "ic_category", "ic_work", "ic_personal", "ic_study", "ic_health",
                "ic_home", "ic_shopping", "ic_event", "ic_fitness", "ic_travel"
        );
        final int[] availableColors = {
                0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF3F51B5,
                0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4, 0xFF009688, 0xFF4CAF50,
                0xFF8BC34A, 0xFFCDDC39, 0xFFFFEB3B, 0xFFFFC107, 0xFFFF9800,
                0xFFFF5722, 0xFF795548, 0xFF9E9E9E, 0xFF607D8B
        };

        for (int color : availableColors) {
            RadioButton rb = new RadioButton(this);
            rb.setButtonDrawable(null);
            rb.setBackgroundResource(R.drawable.bg_color_picker_item);
            rb.setBackgroundTintList(ColorStateList.valueOf(color));
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(80, 80);
            params.setMargins(8, 8, 8, 8);
            rb.setLayoutParams(params);
            if (color == selectedColor[0]) rb.setChecked(true);
            rb.setOnClickListener(v -> {
                selectedColor[0] = color;
                ((IconAdapter)rvIcons.getAdapter()).setCurrentColor(color);
            });
            rgColors.addView(rb);
        }

        IconAdapter iconAdapter = new IconAdapter(availableIcons, selectedColor[0], iconName -> selectedIcon[0] = iconName);
        rvIcons.setLayoutManager(new GridLayoutManager(this, 5));
        rvIcons.setAdapter(iconAdapter);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_custom_category)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        CustomCategory newCat = new CustomCategory(name, selectedIcon[0], selectedColor[0]);
                        dbHelper.insertCustomCategory(newCat);
                        customCategoryName = name;
                    } else {
                        spinnerCategory.setSelection(0);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    if (customCategoryName == null) {
                        spinnerCategory.setSelection(0);
                    }
                })
                .show();
    }

    private void setupSpinners() {
        Priority[] priorities = Priority.values();
        String[] priorityLabels = new String[priorities.length];
        for (int i = 0; i < priorities.length; i++) {
            priorityLabels[i] = getString(priorities[i].getLabelResId());
        }
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, priorityLabels);
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

        com.example.schedulemanager.models.Category[] categories = com.example.schedulemanager.models.Category.values();
        String[] categoryLabels = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            categoryLabels[i] = getString(categories[i].getLabelResId());
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryLabels);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        com.example.schedulemanager.models.Recurrence[] recurrences = com.example.schedulemanager.models.Recurrence.values();
        String[] recurrenceLabels = new String[recurrences.length];
        for (int i = 0; i < recurrences.length; i++) {
            recurrenceLabels[i] = getString(recurrences[i].getLabelResId());
        }
        ArrayAdapter<String> recurrenceAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, recurrenceLabels);
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecurrence.setAdapter(recurrenceAdapter);
    }

    private void setupPickers() {
        btnPickDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            if (isEditMode || selectedDate != null) {
                c = DateTimeUtils.getCalendar(selectedDate, "00:00", this);
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

        currentAttachmentPath = existingSchedule.getAttachmentPath();
        showAttachment(currentAttachmentPath);

        loadSubTasks();
        updateDisplayValues();
    }

    private void loadSubTasks() {
        subTasks.clear();
        subTasks.addAll(dbHelper.getSubTasksForSchedule(existingSchedule.getId()));
        subTaskAdapter.notifyDataSetChanged();
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

        if (DateTimeUtils.isDateInPast(selectedDate, selectedStartTime, this)) {
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
        schedule.setCustomCategory(customCategoryName);
        schedule.setAttachmentPath(currentAttachmentPath);
        schedule.setRecurrence(com.example.schedulemanager.models.Recurrence.fromInt(spinnerRecurrence.getSelectedItemPosition()));
        schedule.setReminderMinutes(getReminderMinutes(spinnerReminder.getSelectedItemPosition()));
        if (!isEditMode) {
            schedule.setCreatedAt(DateTimeUtils.getCurrentDateTime(this));
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
            saveSubTasks(schedule.getId());
            ReminderScheduler.scheduleReminder(this, schedule);
            setResult(RESULT_OK);
            finish();
        } else {
            Snackbar.make(btnSave, R.string.error_saving_schedule, Snackbar.LENGTH_LONG).show();
        }
    }

    private void saveSubTasks(int scheduleId) {
        List<SubTask> existingInDb = dbHelper.getSubTasksForSchedule(scheduleId);
        for (SubTask st : existingInDb) {
            dbHelper.deleteSubTask(st.getId());
        }

        for (SubTask st : subTasks) {
            if (st.getTitle() != null && !st.getTitle().trim().isEmpty()) {
                st.setScheduleId(scheduleId);
                dbHelper.insertSubTask(st);
            }
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
    public void onBackPressed() {
        if (hasUnsavedChanges()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.discard_changes_title)
                    .setMessage(R.string.discard_changes_msg)
                    .setPositiveButton(R.string.yes, (dialog, which) -> super.onBackPressed())
                    .setNegativeButton(R.string.no, null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        
        if (isEditMode) {
            return !title.equals(existingSchedule.getTitle()) ||
                    !description.equals(existingSchedule.getDescription()) ||
                    !selectedDate.equals(existingSchedule.getDate()) ||
                    !selectedStartTime.equals(existingSchedule.getStartTime()) ||
                    !selectedEndTime.equals(existingSchedule.getEndTime()) ||
                    spinnerPriority.getSelectedItemPosition() != existingSchedule.getPriority().toInt() ||
                    spinnerCategory.getSelectedItemPosition() != (existingSchedule.getCategory() != null ? existingSchedule.getCategory().toInt() : 0) ||
                    spinnerRecurrence.getSelectedItemPosition() != (existingSchedule.getRecurrence() != null ? existingSchedule.getRecurrence().toInt() : 0) ||
                    spinnerReminder.getSelectedItemPosition() != getReminderIndex(existingSchedule.getReminderMinutes()) ||
                    !java.util.Objects.equals(currentAttachmentPath, existingSchedule.getAttachmentPath());
        } else {
            return !title.isEmpty() || !description.isEmpty();
        }
    }
}
