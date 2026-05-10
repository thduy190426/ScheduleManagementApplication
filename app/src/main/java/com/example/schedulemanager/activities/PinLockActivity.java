package com.example.schedulemanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.schedulemanager.R;
import com.example.schedulemanager.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class PinLockActivity extends BaseActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final int MODE_UNLOCK = 0;
    public static final int MODE_SETUP = 1;

    private int currentMode = MODE_UNLOCK;
    private PreferenceManager prefManager;
    private String firstPin = "";
    private StringBuilder inputPin = new StringBuilder();

    private TextView tvTitle;
    private List<View> dotViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);

        prefManager = new PreferenceManager(this);
        currentMode = getIntent().getIntExtra(EXTRA_MODE, MODE_UNLOCK);

        tvTitle = findViewById(R.id.tvTitle);
        initDots();
        initKeypad();

        updateUI();
    }

    private void initDots() {
        dotViews = new ArrayList<>();
        dotViews.add(findViewById(R.id.dot1));
        dotViews.add(findViewById(R.id.dot2));
        dotViews.add(findViewById(R.id.dot3));
        dotViews.add(findViewById(R.id.dot4));
    }

    private void initKeypad() {
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        for (int id : buttonIds) {
            View btn = findViewById(id);
            if (btn instanceof TextView) {
                String digit = ((TextView) btn).getText().toString();
                btn.setOnClickListener(v -> addDigit(digit));
            }
        }

        findViewById(R.id.btnDelete).setOnClickListener(v -> removeDigit());
    }

    private void addDigit(String digit) {
        if (inputPin.length() < 4) {
            inputPin.append(digit);
            updateDots();
            if (inputPin.length() == 4) {
                setKeypadEnabled(false);
                findViewById(R.id.layoutDots).postDelayed(() -> {
                    handlePinEntry(inputPin.toString());
                }, 200);
            }
        }
    }

    private void removeDigit() {
        if (inputPin.length() > 0) {
            inputPin.deleteCharAt(inputPin.length() - 1);
            updateDots();
        }
    }

    private void updateDots() {
        for (int i = 0; i < dotViews.size(); i++) {
            if (i < inputPin.length()) {
                dotViews.get(i).setBackgroundResource(R.drawable.pin_dot_filled);
            } else {
                dotViews.get(i).setBackgroundResource(R.drawable.pin_dot_empty);
            }
        }
    }

    private void handlePinEntry(String pin) {
        if (currentMode == MODE_UNLOCK) {
            handleUnlock(pin);
        } else {
            handleSetup(pin);
        }
    }

    private void updateUI() {
        if (currentMode == MODE_UNLOCK) {
            tvTitle.setText(R.string.unlock_title);
        } else {
            tvTitle.setText(firstPin.isEmpty() ? R.string.setup_pin_title : R.string.confirm_pin);
        }
        inputPin.setLength(0);
        updateDots();
        setKeypadEnabled(true);
    }

    private void setKeypadEnabled(boolean enabled) {
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDelete
        };
        for (int id : buttonIds) {
            View btn = findViewById(id);
            if (btn != null) {
                btn.setEnabled(enabled);
                btn.setAlpha(enabled ? 1.0f : 0.5f);
            }
        }
    }

    private void handleUnlock(String pin) {
        String savedPin = prefManager.getAppPin();
        if (pin.equals(savedPin)) {
            Intent intent;
            String shortcutAction = getIntent().getStringExtra("shortcut_action");
            if ("android.intent.action.VIEW".equals(shortcutAction)) {
                intent = new Intent(this, AddEditScheduleActivity.class);
            } else {
                intent = new Intent(this, MainActivity.class);
            }
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, R.string.wrong_pin, Toast.LENGTH_SHORT).show();
            updateUI();
        }
    }

    private void handleSetup(String pin) {
        if (firstPin.isEmpty()) {
            firstPin = pin;
            updateUI();
        } else {
            if (pin.equals(firstPin)) {
                prefManager.setAppPin(pin);
                prefManager.setPinLockEnabled(true);
                Toast.makeText(this, R.string.pin_saved, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.pin_mismatch, Toast.LENGTH_SHORT).show();
                firstPin = "";
                updateUI();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (currentMode == MODE_UNLOCK) {
            finishAffinity();
        } else {
            super.onBackPressed();
        }
    }
}
