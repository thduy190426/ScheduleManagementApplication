package com.example.schedulemanager.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Schedule implements Parcelable {
    private int id;
    private String title;
    private String description;
    private String date;
    private String startTime;
    private String endTime;
    private Priority priority;
    private Category category;
    private Recurrence recurrence;
    private boolean isCompleted;
    private int reminderMinutes;
    private String createdAt;

    public Schedule() {
    }

    protected Schedule(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        date = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        String priorityStr = in.readString();
        priority = priorityStr != null ? Priority.valueOf(priorityStr) : Priority.LOW;
        String categoryStr = in.readString();
        category = categoryStr != null ? Category.valueOf(categoryStr) : Category.OTHER;
        String recurrenceStr = in.readString();
        recurrence = recurrenceStr != null ? Recurrence.valueOf(recurrenceStr) : Recurrence.NONE;
        isCompleted = in.readByte() != 0;
        reminderMinutes = in.readInt();
        createdAt = in.readString();
    }

    public static final Creator<Schedule> CREATOR = new Creator<Schedule>() {
        @Override
        public Schedule createFromParcel(Parcel in) {
            return new Schedule(in);
        }

        @Override
        public Schedule[] newArray(int size) {
            return new Schedule[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(date);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(priority != null ? priority.name() : Priority.LOW.name());
        dest.writeString(category != null ? category.name() : Category.OTHER.name());
        dest.writeString(recurrence != null ? recurrence.name() : Recurrence.NONE.name());
        dest.writeByte((byte) (isCompleted ? 1 : 0));
        dest.writeInt(reminderMinutes);
        dest.writeString(createdAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Recurrence getRecurrence() { return recurrence; }
    public void setRecurrence(Recurrence recurrence) { this.recurrence = recurrence; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getReminderMinutes() { return reminderMinutes; }
    public void setReminderMinutes(int reminderMinutes) { this.reminderMinutes = reminderMinutes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @NonNull
    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", startTime='" + startTime + '\'' +
                ", isCompleted=" + isCompleted +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return id == schedule.id &&
                isCompleted == schedule.isCompleted &&
                reminderMinutes == schedule.reminderMinutes &&
                Objects.equals(title, schedule.title) &&
                Objects.equals(description, schedule.description) &&
                Objects.equals(date, schedule.date) &&
                Objects.equals(startTime, schedule.startTime) &&
                Objects.equals(endTime, schedule.endTime) &&
                priority == schedule.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, date, startTime, endTime, priority, isCompleted, reminderMinutes);
    }

    /**
     * Builder pattern for Schedule
     */
    public static class Builder {
        private final Schedule schedule = new Schedule();

        public Builder id(int id) { schedule.id = id; return this; }
        public Builder title(String title) { schedule.title = title; return this; }
        public Builder description(String description) { schedule.description = description; return this; }
        public Builder date(String date) { schedule.date = date; return this; }
        public Builder startTime(String startTime) { schedule.startTime = startTime; return this; }
        public Builder endTime(String endTime) { schedule.endTime = endTime; return this; }
        public Builder priority(Priority priority) { schedule.priority = priority; return this; }
        public Builder category(Category category) { schedule.category = category; return this; }
        public Builder recurrence(Recurrence recurrence) { schedule.recurrence = recurrence; return this; }
        public Builder isCompleted(boolean isCompleted) { schedule.isCompleted = isCompleted; return this; }
        public Builder reminderMinutes(int reminderMinutes) { schedule.reminderMinutes = reminderMinutes; return this; }
        public Builder createdAt(String createdAt) { schedule.createdAt = createdAt; return this; }

        public Schedule build() { return schedule; }
    }
}
