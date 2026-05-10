package com.example.schedulemanager.models;

import com.example.schedulemanager.R;

public enum Category {
    WORK(R.string.category_work, R.color.category_work),
    PERSONAL(R.string.category_personal, R.color.category_personal),
    STUDY(R.string.category_study, R.color.category_study),
    HEALTH(R.string.category_health, R.color.category_health),
    OTHER(R.string.category_other, R.color.category_other);

    private final int labelResId;
    private final int colorResId;
    private String customLabel;

    Category(int labelResId, int colorResId) {
        this.labelResId = labelResId;
        this.colorResId = colorResId;
    }

    public int getLabelResId() {
        return labelResId;
    }

    public void setCustomLabel(String customLabel) {
        this.customLabel = customLabel;
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
