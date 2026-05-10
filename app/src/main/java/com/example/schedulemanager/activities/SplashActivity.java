package com.example.schedulemanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.schedulemanager.R;
import com.example.schedulemanager.utils.PreferenceManager;

public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.d(TAG, "onCreate");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            PreferenceManager prefManager = new PreferenceManager(this);
            Intent intent;
            
            // Check for App Shortcuts
            String action = getIntent().getAction();
            boolean isShortcut = "android.intent.action.VIEW".equals(action);
            
            if (prefManager.isFirstLaunch()) {
                intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            } else if (prefManager.isPinLockEnabled() && !prefManager.getAppPin().isEmpty()) {
                prefManager.setPinLockEnabled(false);
                intent = new Intent(SplashActivity.this, PinLockActivity.class);
                intent.putExtra(PinLockActivity.EXTRA_MODE, PinLockActivity.MODE_UNLOCK);
                if (isShortcut) {
                    intent.putExtra("shortcut_action", action);
                }
            } else {
                if (isShortcut) {
                    intent = new Intent(SplashActivity.this, AddEditScheduleActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                }
            }
            
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
