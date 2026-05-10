package com.example.schedulemanager.models;

import com.example.schedulemanager.R;

public enum Priority {
    LOW(R.string.priority_low, R.color.priority_low),
    MEDIUM(R.string.priority_medium, R.color.priority_medium),
    HIGH(R.string.priority_high, R.color.priority_high);

    private final int labelResId;
    private final int colorResId;

    Priority(int labelResId, int colorResId) {
        this.labelResId = labelResId;
        this.colorResId = colorResId;
    }

    public int getLabelResId() {
        return labelResId;
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
