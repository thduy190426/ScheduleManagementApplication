package com.example.schedulemanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Window;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemanager.R;
import com.example.schedulemanager.adapters.DetailSubTaskAdapter;
import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.Schedule;
import com.example.schedulemanager.models.SubTask;
import com.example.schedulemanager.utils.DateTimeUtils;
import com.example.schedulemanager.utils.IntentKeys;
import com.example.schedulemanager.utils.ReminderScheduler;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DetailActivity extends BaseActivity {

    private static final String TAG = "DetailActivity";

    private TextView tvPriority, tvTitle, tvDateTime, tvReminder, tvDescription, tvCategory, tvRecurrence, tvFocusTimer;
    private Button btnToggleComplete, btnStartFocus;
    private ImageView ivAttachment;
    private RecyclerView rvSubTasks;
    private View cardSubTasks, cardAttachment;
    private Schedule schedule;
    private DatabaseHelper dbHelper;

    private final ActivityResultLauncher<Intent> editScheduleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    schedule = dbHelper.getScheduleById(schedule.getId());
                    if (schedule != null) {
                        displaySchedule();
                        displaySubTasks();
                    } else {
                        finish();
                    }
                }
            }
    );

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning;
    private boolean isBreakMode = false;
    private static final long WORK_TIME_IN_MILLIS = 1500000;
    private static final long BREAK_TIME_IN_MILLIS = 300000;
    private long timeLeftInMillis = WORK_TIME_IN_MILLIS;
    private long endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        setByIdTransitionName();
        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        getWindow().setSharedElementEnterTransition(buildContainerTransform(true));
        getWindow().setSharedElementReturnTransition(buildContainerTransform(false));

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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvPriority = findViewById(R.id.tvDetailPriority);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvDateTime = findViewById(R.id.tvDetailDateTime);
        tvReminder = findViewById(R.id.tvDetailReminder);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvRecurrence = findViewById(R.id.tvDetailRecurrence);
        btnToggleComplete = findViewById(R.id.btnToggleComplete);
        tvFocusTimer = findViewById(R.id.tvFocusTimer);
        btnStartFocus = findViewById(R.id.btnStartFocus);
        rvSubTasks = findViewById(R.id.rvDetailSubTasks);
        cardSubTasks = findViewById(R.id.cardSubTasks);
        cardAttachment = findViewById(R.id.cardAttachment);
        ivAttachment = findViewById(R.id.ivDetailAttachment);

        if (savedInstanceState != null) {
            timeLeftInMillis = savedInstanceState.getLong("timeLeftInMillis");
            isTimerRunning = savedInstanceState.getBoolean("isTimerRunning");
            isBreakMode = savedInstanceState.getBoolean("isBreakMode");
            updateCountDownText();
            if (isTimerRunning) {
                endTime = savedInstanceState.getLong("endTime");
                timeLeftInMillis = endTime - System.currentTimeMillis();
                if (timeLeftInMillis < 0) {
                    timeLeftInMillis = 0;
                    isTimerRunning = false;
                    updateCountDownText();
                } else {
                    startTimer();
                }
            }
        }

        ivAttachment.setOnClickListener(v -> {
            if (schedule.getAttachmentPath() != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(android.net.Uri.parse(schedule.getAttachmentPath()), "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Cannot open image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rvSubTasks.setLayoutManager(new LinearLayoutManager(this));

        displaySchedule();
        displaySubTasks();

        btnToggleComplete.setOnClickListener(v -> {
            schedule.setCompleted(!schedule.isCompleted());
            dbHelper.updateSchedule(schedule);
            displaySchedule();
        });

        btnStartFocus.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnStartFocus.setOnLongClickListener(v -> {
            resetTimer();
            return true;
        });
    }

    private void startTimer() {
        endTime = System.currentTimeMillis() + timeLeftInMillis;

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                playAlertSound();
                
                if (!isBreakMode) {
                    isBreakMode = true;
                    timeLeftInMillis = BREAK_TIME_IN_MILLIS;
                    Toast.makeText(DetailActivity.this, R.string.focus_finished_msg, Toast.LENGTH_LONG).show();
                    
                    if (!schedule.isCompleted()) {
                        schedule.setCompleted(true);
                        dbHelper.updateSchedule(schedule);
                        displaySchedule();
                    }
                } else {
                    isBreakMode = false;
                    timeLeftInMillis = WORK_TIME_IN_MILLIS;
                    Toast.makeText(DetailActivity.this, R.string.break_finished_msg, Toast.LENGTH_LONG).show();
                }
                
                updateCountDownText();
                btnStartFocus.setText(R.string.start_focus);
            }
        }.start();

        isTimerRunning = true;
        btnStartFocus.setText(isBreakMode ? R.string.stop_break : R.string.stop_focus);
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        btnStartFocus.setText(R.string.start_focus);
    }

    private void resetTimer() {
        pauseTimer();
        isBreakMode = false;
        timeLeftInMillis = WORK_TIME_IN_MILLIS;
        updateCountDownText();
        Toast.makeText(this, "Timer reset", Toast.LENGTH_SHORT).show();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvFocusTimer.setText(timeLeftFormatted);
        
        tvFocusTimer.setTextColor(isBreakMode ? 
                ContextCompat.getColor(this, R.color.green) : 
                com.google.android.material.color.MaterialColors.getColor(tvFocusTimer, com.google.android.material.R.attr.colorPrimary));
    }

    private void playAlertSound() {
        try {
            android.media.ToneGenerator toneG = new android.media.ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100);
            toneG.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 2000);
        } catch (Exception e) {
            Log.e(TAG, "Error playing sound", e);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("timeLeftInMillis", timeLeftInMillis);
        outState.putBoolean("isTimerRunning", isTimerRunning);
        outState.putBoolean("isBreakMode", isBreakMode);
        outState.putLong("endTime", endTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void setByIdTransitionName() {
        schedule = getIntent().getParcelableExtra(IntentKeys.EXTRA_SCHEDULE);
        if (schedule != null) {
            findViewById(android.R.id.content).setTransitionName("schedule_card_" + schedule.getId());
        }
    }

    private MaterialContainerTransform buildContainerTransform(boolean entering) {
        MaterialContainerTransform transform = new MaterialContainerTransform();
        transform.setTransitionDirection(entering ? MaterialContainerTransform.TRANSITION_DIRECTION_ENTER : MaterialContainerTransform.TRANSITION_DIRECTION_RETURN);
        transform.setAllContainerColors(com.google.android.material.color.MaterialColors.getColor(findViewById(android.R.id.content), com.google.android.material.R.attr.colorSurface));
        transform.addTarget(android.R.id.content);
        transform.setDuration(450);
        return transform;
    }

    private void displaySchedule() {
        tvTitle.setText(schedule.getTitle());
        tvPriority.setText(getString(schedule.getPriority().getLabelResId()));
        tvPriority.setBackgroundTintList(ContextCompat.getColorStateList(this, schedule.getPriority().getColorResId()));

        if (schedule.getCategory() != null) {
            String label = getString(schedule.getCategory().getLabelResId());
            if (schedule.getCategory() == com.example.schedulemanager.models.Category.OTHER && 
                    schedule.getCustomCategory() != null && !schedule.getCustomCategory().isEmpty()) {
                label = schedule.getCustomCategory();
            }
            tvCategory.setText(label);
            tvCategory.setVisibility(android.view.View.VISIBLE);
        } else {
            tvCategory.setVisibility(android.view.View.GONE);
        }

        if (schedule.getRecurrence() != null && schedule.getRecurrence() != com.example.schedulemanager.models.Recurrence.NONE) {
            tvRecurrence.setText(getString(R.string.repeats_label, getString(schedule.getRecurrence().getLabelResId())));
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

        if (schedule.getAttachmentPath() != null && !schedule.getAttachmentPath().isEmpty()) {
            cardAttachment.setVisibility(View.VISIBLE);
            try {
                ivAttachment.setImageURI(android.net.Uri.parse(schedule.getAttachmentPath()));
            } catch (Exception e) {
                Log.e(TAG, "Error loading attachment", e);
                cardAttachment.setVisibility(View.GONE);
            }
        } else {
            cardAttachment.setVisibility(View.GONE);
        }

        displaySubTasks();

        btnToggleComplete.setText(schedule.isCompleted() ? getString(R.string.mark_incomplete) : getString(R.string.mark_completed));
    }

    private void displaySubTasks() {
        java.util.List<SubTask> subTasks = dbHelper.getSubTasksForSchedule(schedule.getId());
        if (subTasks != null && !subTasks.isEmpty()) {
            cardSubTasks.setVisibility(View.VISIBLE);
            DetailSubTaskAdapter adapter = new DetailSubTaskAdapter(subTasks, (subTask, isCompleted) -> {
                dbHelper.updateSubTask(subTask);
            });
            rvSubTasks.setAdapter(adapter);
        } else {
            cardSubTasks.setVisibility(View.GONE);
        }
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
            editScheduleLauncher.launch(intent);
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
