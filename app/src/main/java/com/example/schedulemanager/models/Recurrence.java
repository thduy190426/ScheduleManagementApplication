package com.example.schedulemanager.models;

public enum Recurrence {
    NONE("None"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");

    private final String label;

    Recurrence(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
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
