package com.example.schedulemanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemanager.R;
import com.example.schedulemanager.adapters.ScheduleAdapter;
import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.Schedule;

import java.util.List;

public class TrashActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private DatabaseHelper dbHelper;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.nav_trash);
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

        dbHelper = DatabaseHelper.getInstance(this);
        recyclerView = findViewById(R.id.recyclerViewTrash);
        tvEmpty = findViewById(R.id.tvEmptyTrash);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new ScheduleAdapter(new ScheduleAdapter.OnScheduleClickListener() {
            @Override
            public void onScheduleClick(Schedule schedule, View view) {
            }

            @Override
            public void onScheduleCheckChanged(Schedule schedule, boolean isChecked) {
                loadTrashSchedules(); 
            }

            @Override
            public void onScheduleLongClick(Schedule schedule) {
                showTrashOptions(schedule);
            }
        });
        
        recyclerView.setAdapter(adapter);
        loadTrashSchedules();
    }

    private void loadTrashSchedules() {
        List<Schedule> trashList = dbHelper.getTrashSchedules();
        if (trashList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.submitList(trashList);
        }
    }

    private void showTrashOptions(Schedule schedule) {
        String[] options = {getString(R.string.restore), getString(R.string.delete_permanently)};
        new AlertDialog.Builder(this)
                .setTitle(schedule.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        dbHelper.restoreSchedule(schedule.getId());
                        Toast.makeText(this, R.string.restore_success, Toast.LENGTH_SHORT).show();
                        loadTrashSchedules();
                    } else {
                        showDeleteForeverConfirm(schedule);
                    }
                })
                .show();
    }

    private void showDeleteForeverConfirm(Schedule schedule) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_permanently)
                .setMessage(R.string.delete_forever_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    dbHelper.deleteForever(schedule.getId());
                    loadTrashSchedules();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
