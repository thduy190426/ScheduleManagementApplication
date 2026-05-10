package com.example.schedulemanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.schedulemanager.R;
import com.example.schedulemanager.utils.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class OnboardingActivity extends BaseActivity {

    private static final String TAG = "OnboardingActivity";

    private TextInputLayout nameInputLayout;
    private TextInputEditText nameEditText;
    private Button getStartedButton;
    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        prefManager = new PreferenceManager(this);

        nameInputLayout = findViewById(R.id.nameInputLayout);
        nameEditText = findViewById(R.id.nameEditText);
        getStartedButton = findViewById(R.id.getStartedButton);

        getStartedButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            if (name.isEmpty()) {
                nameInputLayout.setError(getString(R.string.name_required));
            } else {
                prefManager.setUserName(name);
                prefManager.setFirstLaunch(false);
                navigateToMain();
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
