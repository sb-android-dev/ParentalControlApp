package com.schoolmanager.model;

public class TrackingHistoryItem {

    private String title;
    private String subTitle;
    private String anchor;
    private boolean isActive;

    public TrackingHistoryItem() {
    }

    public TrackingHistoryItem(String title, String subTitle, String anchor, boolean isActive) {
        this.title = title;
        this.subTitle = subTitle;
        this.anchor = anchor;
        this.isActive = isActive;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getAnchor() {
        return anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
