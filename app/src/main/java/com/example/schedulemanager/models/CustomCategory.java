package com.example.schedulemanager.models;

public class CustomCategory {
    private int id;
    private String name;
    private String iconName;
    private int color;

    public CustomCategory() {}

    public CustomCategory(String name, String iconName, int color) {
        this.name = name;
        this.iconName = iconName;
        this.color = color;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
}
