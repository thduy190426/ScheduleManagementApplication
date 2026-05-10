package com.example.schedulemanager.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemanager.R;
import com.example.schedulemanager.activities.DetailActivity;
import com.example.schedulemanager.adapters.ScheduleAdapter;
import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.Schedule;
import com.example.schedulemanager.utils.PreferenceManager;
import com.example.schedulemanager.utils.SwipeCallbacks;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;

public class AllSchedulesFragment extends Fragment implements ScheduleAdapter.OnScheduleClickListener, SearchableFragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ScheduleAdapter adapter;
    private DatabaseHelper dbHelper;
    private ChipGroup chipGroupFilters;

    private String currentQuery = "";
    private int currentStatus = -1;
    private int currentPriority = -1;
    private int currentCategory = -1;
    private String currentSortBy = "time";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = DatabaseHelper.getInstance(requireContext());
        currentSortBy = new PreferenceManager(requireContext()).getDefaultSortOrder();
        adapter = new ScheduleAdapter(this);
        recyclerView.setAdapter(adapter);

        setupSwipeActions();
        setupFilters();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSchedules();
    }

    private void setupFilters() {
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

    private void setupSwipeActions() {
        new ItemTouchHelper(new SwipeCallbacks(requireContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Schedule schedule = adapter.getCurrentList().get(position);

                if (direction == ItemTouchHelper.LEFT) {
                    dbHelper.deleteSchedule(schedule.getId());
                    loadSchedules();
                    Snackbar.make(recyclerView, R.string.schedule_deleted, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, v -> {
                                dbHelper.addSchedule(schedule);
                                loadSchedules();
                            }).show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    schedule.setCompleted(!schedule.isCompleted());
                    dbHelper.updateSchedule(schedule);
                    loadSchedules();
                    String message = schedule.isCompleted() ?
                            getString(R.string.mark_completed) : getString(R.string.mark_incomplete);
                    Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT).show();
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void loadSchedules() {
        if (!isAdded()) return;
        List<Schedule> schedules = dbHelper.searchSchedules(currentQuery, currentCategory, currentStatus, currentPriority, currentSortBy, null);
        adapter.submitList(schedules);
        tvEmpty.setVisibility(schedules.isEmpty() ? View.VISIBLE : View.GONE);
        if (schedules.isEmpty() && !currentQuery.isEmpty()) {
            tvEmpty.setText(R.string.no_search_results);
        } else {
            tvEmpty.setText(R.string.no_schedules);
        }
    }

    @Override
    public void onSearch(String query) {
        this.currentQuery = query;
        loadSchedules();
    }

    @Override
    public void onSortRequested() {
        showFilterDialog();
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_advanced_filter, null);

        com.google.android.material.textfield.MaterialAutoCompleteTextView spinnerPriority = dialogView.findViewById(R.id.spinnerFilterPriority);
        com.google.android.material.textfield.MaterialAutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinnerFilterCategory);
        com.google.android.material.textfield.MaterialAutoCompleteTextView spinnerSort = dialogView.findViewById(R.id.spinnerFilterSort);

        String[] priorities = {getString(R.string.all), getString(R.string.priority_low), getString(R.string.priority_medium), getString(R.string.priority_high)};
        android.widget.ArrayAdapter<String> pAdapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, priorities);
        spinnerPriority.setAdapter(pAdapter);
        spinnerPriority.setText(priorities[currentPriority + 1], false);

        String[] categories = {getString(R.string.all), getString(R.string.category_work), getString(R.string.category_personal), getString(R.string.category_study), getString(R.string.category_health), getString(R.string.category_other)};
        android.widget.ArrayAdapter<String> cAdapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(cAdapter);
        spinnerCategory.setText(categories[currentCategory + 1], false);

        String[] sorts = {getString(R.string.sort_time), getString(R.string.sort_priority), getString(R.string.sort_title)};
        android.widget.ArrayAdapter<String> sAdapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, sorts);
        spinnerSort.setAdapter(sAdapter);
        String currentSortLabel;
        if (currentSortBy.equals("priority")) currentSortLabel = getString(R.string.sort_priority);
        else if (currentSortBy.equals("title")) currentSortLabel = getString(R.string.sort_title);
        else currentSortLabel = getString(R.string.sort_time);
        spinnerSort.setText(currentSortLabel, false);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.advanced_filters)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String pSel = spinnerPriority.getText().toString();
                    currentPriority = pSel.equals(getString(R.string.all)) ? -1 : Arrays.asList(priorities).indexOf(pSel) - 1;

                    String cSel = spinnerCategory.getText().toString();
                    currentCategory = cSel.equals(getString(R.string.all)) ? -1 : Arrays.asList(categories).indexOf(cSel) - 1;

                    String sSel = spinnerSort.getText().toString();
                    if (sSel.equals(getString(R.string.sort_priority))) currentSortBy = "priority";
                    else if (sSel.equals(getString(R.string.sort_title))) currentSortBy = "title";
                    else currentSortBy = "time";

                    loadSchedules();
                })
                .setNeutralButton(R.string.reset, (dialog, which) -> {
                    currentPriority = -1;
                    currentCategory = -1;
                    currentSortBy = "time";
                    currentStatus = -1;
                    chipGroupFilters.check(R.id.chipAll);
                    loadSchedules();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onScheduleClick(Schedule schedule, View view) {
        Intent intent = new Intent(requireContext(), DetailActivity.class);
        intent.putExtra(com.example.schedulemanager.utils.IntentKeys.EXTRA_SCHEDULE, schedule);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), view, view.getTransitionName());
        startActivity(intent, options.toBundle());
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
