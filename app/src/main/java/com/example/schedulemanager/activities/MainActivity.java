package com.example.schedulemanager.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.schedulemanager.R;
import com.example.schedulemanager.fragments.AllSchedulesFragment;
import com.example.schedulemanager.fragments.SearchableFragment;
import com.example.schedulemanager.fragments.TodayFragment;
import com.example.schedulemanager.fragments.WeeklyFragment;
import com.example.schedulemanager.utils.PreferenceManager;
import com.example.schedulemanager.utils.QuickAddParser;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawerLayout;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private boolean shouldOpenDrawerOnResume = false;

    private final ActivityResultLauncher<Intent> subActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                shouldOpenDrawerOnResume = true;
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Notification permission granted!");
                } else {
                    Log.d(TAG, "Notification permission denied!");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager prefManager = new PreferenceManager(this);
        checkNotificationPermission();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_home, R.string.nav_home);
        toggle.getDrawerArrowDrawable().setColor(Color.WHITE);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getIntent().getBooleanExtra("OPEN_DRAWER", false)) {
            drawerLayout.openDrawer(GravityCompat.START);
        }

        TextView navHeaderName = navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);
        if (navHeaderName != null) {
            String userName = prefManager.getUserName();
            navHeaderName.setText(getString(R.string.nav_header_greeting, userName));
        }

        setupDarkModeSwitch(navigationView, prefManager);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        setupViewPager();

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditScheduleActivity.class);
            subActivityLauncher.launch(intent);
        });

        findViewById(R.id.fabAdd).setOnLongClickListener(v -> {
            showQuickAddDialog();
            return true;
        });
    }

    private void showQuickAddDialog() {
        EditText input = new EditText(this);
        input.setHint(R.string.quick_add_hint);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle(R.string.quick_add_title)
                .setView(input)
                .setPositiveButton(R.string.quick_add_positive, (dialog, which) -> {
                    String text = input.getText().toString();
                    if (!text.isEmpty()) {
                        processQuickAdd(text);
                    }
                })
                .setNegativeButton(R.string.quick_add_negative, null)
                .show();
    }

    private void processQuickAdd(String text) {
        PreferenceManager prefManager = new PreferenceManager(this);
        String language = prefManager.getLanguage();
        QuickAddParser.Result result = QuickAddParser.parse(text, language);
        
        Intent intent = new Intent(this, AddEditScheduleActivity.class);
        intent.putExtra(com.example.schedulemanager.utils.IntentKeys.EXTRA_QUICK_ADD_TITLE, result.title);
        intent.putExtra(com.example.schedulemanager.utils.IntentKeys.EXTRA_QUICK_ADD_TIME, result.calendar.getTimeInMillis());
        subActivityLauncher.launch(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldOpenDrawerOnResume) {
            if (drawerLayout != null) {
                drawerLayout.postDelayed(() -> drawerLayout.openDrawer(GravityCompat.START), 100);
            }
            shouldOpenDrawerOnResume = false;
        }
    }

    private void setupDarkModeSwitch(NavigationView navigationView, PreferenceManager prefManager) {
        MenuItem darkModeItem = navigationView.getMenu().findItem(R.id.nav_dark_mode);
        SwitchCompat darkModeSwitch = darkModeItem.getActionView().findViewById(R.id.drawer_switch);

        boolean isDarkMode = prefManager.getThemeMode() == AppCompatDelegate.MODE_NIGHT_YES;
        darkModeSwitch.setChecked(isDarkMode);

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int mode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            prefManager.setThemeMode(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
        });

        darkModeItem.setOnMenuItemClickListener(item -> {
            darkModeSwitch.setChecked(!darkModeSwitch.isChecked());
            return true;
        });
    }

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 1: return new WeeklyFragment();
                    case 2: return new AllSchedulesFragment();
                    default: return new TodayFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText(R.string.tab_today); break;
                case 1: tab.setText(R.string.tab_weekly); break;
                case 2: tab.setText(R.string.tab_all); break;
            }
        }).attach();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                searchView.setQueryHint(getString(R.string.search_hint));

                @SuppressLint("DiscouragedApi") int closeButtonId = searchView.getContext().getResources().getIdentifier("androidx.appcompat:id/search_close_btn", null, null);
                android.widget.ImageView closeButton = searchView.findViewById(closeButtonId);
                if (closeButton != null) {
                    closeButton.setColorFilter(Color.WHITE);
                }

                searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
                        if (toolbar.getNavigationIcon() != null) {
                            toolbar.getNavigationIcon().setTint(Color.WHITE);
                        }
                        toolbar.setNavigationOnClickListener(v -> searchItem.collapseActionView());
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

                        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                                MainActivity.this, drawerLayout, toolbar, R.string.nav_home, R.string.nav_home);
                        toggle.getDrawerArrowDrawable().setColor(Color.WHITE);
                        drawerLayout.addDrawerListener(toggle);
                        toggle.syncState();
                        return true;
                    }
                });

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        dispatchSearch(query);
                        searchView.clearFocus();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        dispatchSearch(newText);
                        return true;
                    }
                });
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
            if (currentFragment instanceof SearchableFragment) {
                ((SearchableFragment) currentFragment).onSortRequested();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dispatchSearch(String query) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (currentFragment instanceof SearchableFragment) {
            ((SearchableFragment) currentFragment).onSearch(query);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_calendar) {
            intent = new Intent(this, CalendarActivity.class);
        } else if (id == R.id.nav_statistics) {
            intent = new Intent(this, StatisticsActivity.class);
        } else if (id == R.id.nav_settings) {
            intent = new Intent(this, SettingsActivity.class);
        } else if (id == R.id.nav_backup) {
            intent = new Intent(this, BackupActivity.class);
        } else if (id == R.id.nav_trash) {
            intent = new Intent(this, TrashActivity.class);
        }

        if (intent != null) {
            subActivityLauncher.launch(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


}
