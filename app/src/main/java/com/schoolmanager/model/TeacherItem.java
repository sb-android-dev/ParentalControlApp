package com.schoolmanager.model;

public class TeacherItem {

    private int teacherId;
    private String teacherName;
    private String subjectName;
    private String teacherPhoneNo;
    private String teacherImage;
    private long teacherLastSeen;
    private boolean isLastSeenEnabled;
    private boolean isReadUnreadEnabled;

    public TeacherItem() {
    }

    public TeacherItem(int teacherId, String teacherName, String subjectName, String teacherPhoneNo,
                       String teacherImage, long teacherLastSeen, boolean isLastSeenEnabled,
                       boolean isReadUnreadEnabled) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.subjectName = subjectName;
        this.teacherPhoneNo = teacherPhoneNo;
        this.teacherImage = teacherImage;
        this.teacherLastSeen = teacherLastSeen;
        this.isLastSeenEnabled = isLastSeenEnabled;
        this.isReadUnreadEnabled = isReadUnreadEnabled;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getTeacherPhoneNo() {
        return teacherPhoneNo;
    }

    public void setTeacherPhoneNo(String teacherPhoneNo) {
        this.teacherPhoneNo = teacherPhoneNo;
    }

    public String getTeacherImage() {
        return teacherImage;
    }

    public void setTeacherImage(String teacherImage) {
        this.teacherImage = teacherImage;
    }

    public long getTeacherLastSeen() {
        return teacherLastSeen;
    }

    public void setTeacherLastSeen(long teacherLastSeen) {
        this.teacherLastSeen = teacherLastSeen;
    }

    public boolean isLastSeenEnabled() {
        return isLastSeenEnabled;
    }

    public void setLastSeenEnabled(boolean lastSeenEnabled) {
        isLastSeenEnabled = lastSeenEnabled;
    }

    public boolean isReadUnreadEnabled() {
        return isReadUnreadEnabled;
    }

    public void setReadUnreadEnabled(boolean readUnreadEnabled) {
        isReadUnreadEnabled = readUnreadEnabled;
    }
}
