package com.example.schedulemanager.models;

import com.example.schedulemanager.R;

public enum Priority {
    LOW("Low", R.color.priority_low),
    MEDIUM("Medium", R.color.priority_medium),
    HIGH("High", R.color.priority_high);

    private final String label;
    private final int colorResId;

    Priority(String label, int colorResId) {
        this.label = label;
        this.colorResId = colorResId;
    }

    public String getLabel() {
        return label;
    }

    public int getColorResId() {
        return colorResId;
    }

    public static Priority fromInt(int value) {
        switch (value) {
            case 0: return LOW;
            case 1: return MEDIUM;
            case 2: return HIGH;
            default: return LOW;
        }
    }

    public int toInt() {
        switch (this) {
            case LOW: return 0;
            case MEDIUM: return 1;
            case HIGH: return 2;
            default: return 0;
        }
    }
}
