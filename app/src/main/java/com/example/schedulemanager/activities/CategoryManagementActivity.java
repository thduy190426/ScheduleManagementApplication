package com.example.schedulemanager.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemanager.R;
import com.example.schedulemanager.adapters.CategoryAdapter;
import com.example.schedulemanager.adapters.IconAdapter;
import com.example.schedulemanager.database.DatabaseHelper;
import com.example.schedulemanager.models.CustomCategory;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryManagementActivity extends BaseActivity {

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private List<CustomCategory> customCategories;
    private DatabaseHelper dbHelper;

    private final List<String> availableIcons = Arrays.asList(
            "ic_category", "ic_work", "ic_personal", "ic_study", "ic_health", 
            "ic_home", "ic_shopping", "ic_event", "ic_fitness", "ic_travel"
    );

    private final int[] availableColors = {
            0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF3F51B5,
            0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4, 0xFF009688, 0xFF4CAF50,
            0xFF8BC34A, 0xFFCDDC39, 0xFFFFEB3B, 0xFFFFC107, 0xFFFF9800,
            0xFFFF5722, 0xFF795548, 0xFF9E9E9E, 0xFF607D8B
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        dbHelper = DatabaseHelper.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvCategories = findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        loadCategories();
    }

    private void loadCategories() {
        customCategories = dbHelper.getAllCustomCategories();
        adapter = new CategoryAdapter(customCategories, new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(CustomCategory category, int position) {
                showEditDialog(category, position);
            }

            @Override
            public void onDelete(CustomCategory category, int position) {
                showDeleteConfirmDialog(category, position);
            }
        });
        rvCategories.setAdapter(adapter);
    }

    private void showEditDialog(CustomCategory category, int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_category, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etCategoryName);
        RadioGroup rgColors = dialogView.findViewById(R.id.rgColors);
        RecyclerView rvIcons = dialogView.findViewById(R.id.rvIcons);

        etName.setText(category.getName());

        final int[] selectedColor = {category.getColor()};
        final String[] selectedIcon = {category.getIconName()};

        for (int color : availableColors) {
            RadioButton rb = new RadioButton(this);
            rb.setButtonDrawable(null);
            rb.setBackgroundResource(R.drawable.bg_color_picker_item);
            rb.setBackgroundTintList(ColorStateList.valueOf(color));
            
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(80, 80);
            params.setMargins(8, 8, 8, 8);
            rb.setLayoutParams(params);
            
            if (color == category.getColor()) rb.setChecked(true);
            
            rb.setOnClickListener(v -> {
                selectedColor[0] = color;
                ((IconAdapter)rvIcons.getAdapter()).setCurrentColor(color);
            });
            rgColors.addView(rb);
        }

        IconAdapter iconAdapter = new IconAdapter(availableIcons, selectedColor[0], iconName -> {
            selectedIcon[0] = iconName;
        });
        iconAdapter.setSelectedIcon(category.getIconName());
        rvIcons.setLayoutManager(new GridLayoutManager(this, 5));
        rvIcons.setAdapter(iconAdapter);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.edit_category)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        category.setName(newName);
                        category.setColor(selectedColor[0]);
                        category.setIconName(selectedIcon[0]);
                        dbHelper.insertCustomCategory(category);
                        loadCategories();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteConfirmDialog(CustomCategory category, int position) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_category)
                .setMessage(R.string.delete_category_msg)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    dbHelper.deleteCustomCategory(category.getName());
                    customCategories.remove(position);
                    adapter.notifyItemRemoved(position);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
