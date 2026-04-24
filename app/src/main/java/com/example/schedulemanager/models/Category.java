package com.example.schedulemanager.models;

import com.example.schedulemanager.R;

public enum Category {
    WORK("Work", R.color.category_work),
    PERSONAL("Personal", R.color.category_personal),
    STUDY("Study", R.color.category_study),
    HEALTH("Health", R.color.category_health),
    OTHER("Other", R.color.category_other);

    private final String label;
    private final int colorResId;

    Category(String label, int colorResId) {
        this.label = label;
        this.colorResId = colorResId;
    }

    public String getLabel() {
        return label;
    }

    public int getColorResId() {
        return colorResId;
    }

    public static Category fromInt(int value) {
        if (value >= 0 && value < values().length) {
            return values()[value];
        }
        return OTHER;
    }

    public int toInt() {
        return ordinal();
    }
}
