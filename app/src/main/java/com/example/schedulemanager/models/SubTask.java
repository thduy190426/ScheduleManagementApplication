package com.example.schedulemanager.models;

import android.os.Parcel;
import android.os.Parcelable;

public class SubTask implements Parcelable {
    private int id;
    private int scheduleId;
    private String title;
    private boolean isCompleted;

    public SubTask() {
    }

    public SubTask(int scheduleId, String title, boolean isCompleted) {
        this.scheduleId = scheduleId;
        this.title = title;
        this.isCompleted = isCompleted;
    }

    protected SubTask(Parcel in) {
        id = in.readInt();
        scheduleId = in.readInt();
        title = in.readString();
        isCompleted = in.readByte() != 0;
    }

    public static final Creator<SubTask> CREATOR = new Creator<SubTask>() {
        @Override
        public SubTask createFromParcel(Parcel in) {
            return new SubTask(in);
        }

        @Override
        public SubTask[] newArray(int size) {
            return new SubTask[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(scheduleId);
        dest.writeString(title);
        dest.writeByte((byte) (isCompleted ? 1 : 0));
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
