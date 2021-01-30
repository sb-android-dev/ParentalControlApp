package com.schoolmanager.model;

public class ScanItem {

    private long scanId;
    private String userId;
    private String userToken;
    private String userType;
    private String studentId;
    private String trackStatus;
    private String trackTime;

    public ScanItem() {
    }

    public ScanItem(long scanId, String userId, String userToken, String userType,
                    String studentId, String trackStatus, String trackTime) {
        this.scanId = scanId;
        this.userId = userId;
        this.userToken = userToken;
        this.userType = userType;
        this.studentId = studentId;
        this.trackStatus = trackStatus;
        this.trackTime = trackTime;
    }

    public long getScanId() {
        return scanId;
    }

    public void setScanId(long scanId) {
        this.scanId = scanId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTrackStatus() {
        return trackStatus;
    }

    public void setTrackStatus(String trackStatus) {
        this.trackStatus = trackStatus;
    }

    public String getTrackTime() {
        return trackTime;
    }

    public void setTrackTime(String trackTime) {
        this.trackTime = trackTime;
    }
}
