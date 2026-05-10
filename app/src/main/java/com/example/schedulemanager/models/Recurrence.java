package com.example.schedulemanager.models;

import com.example.schedulemanager.R;

public enum Recurrence {
    NONE(R.string.repeat_none),
    DAILY(R.string.repeat_daily),
    WEEKLY(R.string.repeat_weekly),
    MONTHLY(R.string.repeat_monthly);

    private final int labelResId;

    Recurrence(int labelResId) {
        this.labelResId = labelResId;
    }

    public int getLabelResId() {
        return labelResId;
    }

    public static Recurrence fromInt(int value) {
        if (value >= 0 && value < values().length) {
            return values()[value];
        }
        return NONE;
    }

    public int toInt() {
        return ordinal();
    }
}
