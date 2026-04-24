package com.example.schedulemanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.schedulemanager.R;
import com.example.schedulemanager.utils.BackupManager;
import com.example.schedulemanager.utils.DateTimeUtils;
import com.example.schedulemanager.utils.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.stream.Collectors;

public class BackupActivity extends AppCompatActivity {

    private static final String TAG = "BackupActivity";

    private TextView tvLastBackup;
    private Button btnExport, btnImport;
    private BackupManager backupManager;
    private PreferenceManager prefManager;

    private final ActivityResultLauncher<Intent> exportLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    handleExport(uri);
                }
            }
    );

    private final ActivityResultLauncher<Intent> importLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    handleImport(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        Log.d(TAG, "onCreate");

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        backupManager = new BackupManager(this);
        prefManager = new PreferenceManager(this);

        tvLastBackup = findViewById(R.id.tvLastBackup);
        btnExport = findViewById(R.id.btnExport);
        btnImport = findViewById(R.id.btnImport);

        updateLastBackupText();

        btnExport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "schedules_backup.json");
            exportLauncher.launch(intent);
        });

        btnImport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            importLauncher.launch(intent);
        });
    }

    private void updateLastBackupText() {
        String lastBackup = prefManager.getLastOpenedDate();
        if (lastBackup.isEmpty()) {
            tvLastBackup.setText(getString(R.string.last_backup, getString(R.string.backup_never)));
        } else {
            tvLastBackup.setText(getString(R.string.last_backup, lastBackup));
        }
    }

    private void handleExport(Uri uri) {
        try {
            String json = backupManager.exportToJSON();
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                if (outputStream != null) {
                    outputStream.write(json.getBytes());
                } else {
                    throw new IOException("Failed to open output stream");
                }
            }
            prefManager.setLastOpenedDate(DateTimeUtils.getCurrentDateTime());
            updateLastBackupText();
            Snackbar.make(btnExport, R.string.backup_success, Snackbar.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(TAG, "Export failed", e);
            Snackbar.make(btnExport, R.string.backup_failed, Snackbar.LENGTH_LONG).show();
        }
    }

    private void handleImport(Uri uri) {
        try {
            String json;
            try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                json = reader.lines().collect(Collectors.joining("\n"));
            }
            
            backupManager.importFromJSON(json);
            Snackbar.make(btnImport, R.string.restore_success, Snackbar.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(TAG, "Import failed", e);
            Snackbar.make(btnImport, R.string.restore_failed, Snackbar.LENGTH_LONG).show();
        }
    }
}
