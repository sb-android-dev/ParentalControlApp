package com.schoolmanager.model;

public class ThemeItem {

    private String themeName;
    private int themeValue;
    private boolean isSelected;

    public ThemeItem() {
    }

    public ThemeItem(String themeName, int themeValue, boolean isSelected) {
        this.themeName = themeName;
        this.themeValue = themeValue;
        this.isSelected = isSelected;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public int getThemeValue() {
        return themeValue;
    }

    public void setThemeValue(int themeValue) {
        this.themeValue = themeValue;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
