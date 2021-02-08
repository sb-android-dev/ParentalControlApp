package com.schoolmanager.model;

public class LanguageItem {

    private String langName;
    private String langCode;
    private String langCountry;
    private boolean isSelected;

    public LanguageItem() {
    }

    public LanguageItem(String langName, String langCode, String langCountry, boolean isSelected) {
        this.langName = langName;
        this.langCode = langCode;
        this.langCountry = langCountry;
        this.isSelected = isSelected;
    }

    public String getLangName() {
        return langName;
    }

    public void setLangName(String langName) {
        this.langName = langName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public String getLangCountry() {
        return langCountry;
    }

    public void setLangCountry(String langCountry) {
        this.langCountry = langCountry;
    }
}
