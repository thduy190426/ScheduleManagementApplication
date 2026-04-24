package com.example.schedulemanager.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemanager.R;
import com.example.schedulemanager.activities.DetailActivity;
import com.example.schedulemanager.adapters.ScheduleAdapter;
import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.Schedule;
import com.example.schedulemanager.utils.DateTimeUtils;
import com.example.schedulemanager.utils.IntentKeys;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.widget.SearchView;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class TodayFragment extends Fragment implements ScheduleAdapter.OnScheduleClickListener {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ScheduleAdapter adapter;
    private DatabaseHelper dbHelper;
    private SearchView searchView;
    private ChipGroup chipGroupFilters;

    private String currentQuery = "";
    private int currentStatus = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        searchView = view.findViewById(R.id.searchView);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = DatabaseHelper.getInstance(requireContext());
        adapter = new ScheduleAdapter(this);
        recyclerView.setAdapter(adapter);

        setupSwipeToDelete();
        setupSearchAndFilters();
    }

    private void setupSearchAndFilters() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                loadSchedules();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                loadSchedules();
                return true;
            }
        });

        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentStatus = -1;
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chipAll) currentStatus = -1;
                else if (id == R.id.chipPending) currentStatus = 0;
                else if (id == R.id.chipCompleted) currentStatus = 1;
            }
            loadSchedules();
        });
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Schedule schedule = adapter.getCurrentList().get(position);

                dbHelper.deleteSchedule(schedule.getId());
                loadSchedules();

                Snackbar.make(recyclerView, R.string.schedule_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            dbHelper.addSchedule(schedule);
                            loadSchedules();
                        }).show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSchedules();
    }

    private void loadSchedules() {
        String today = DateTimeUtils.getCurrentDate();
        List<Schedule> allSchedules = dbHelper.searchSchedules(currentQuery, -1, currentStatus);
        List<Schedule> todaySchedules = new ArrayList<>();
        
        for (Schedule s : allSchedules) {
            if (s.getDate().equals(today)) {
                todaySchedules.add(s);
            }
        }
        
        adapter.submitList(todaySchedules);
        tvEmpty.setVisibility(todaySchedules.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onScheduleClick(Schedule schedule) {
        Intent intent = new Intent(requireContext(), DetailActivity.class);
        intent.putExtra(IntentKeys.EXTRA_SCHEDULE, schedule);
        startActivity(intent);
    }

    @Override
    public void onScheduleCheckChanged(Schedule schedule, boolean isChecked) {
        new Thread(() -> {
            dbHelper.updateSchedule(schedule);
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::loadSchedules);
            }
        }).start();
    }
}
