package com.schoolmanager.model;

public class TeacherItem {

    private int teacherId;
    private String teacherName;
    private String subjectName;
    private String teacherPhoneNo;
    private String teacher_profile;
    private String teacher_last_seen;
    private String teacher_last_seen_permission;
    private String teacher_message_read_permission;



    public TeacherItem() {
    }

    public TeacherItem(int teacherId, String teacherName, String subjectName, String teacherPhoneNo,
                       String teacher_profile,
                       String teacher_last_seen,
                       String teacher_last_seen_permission,
                       String teacher_message_read_permission) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.subjectName = subjectName;
        this.teacherPhoneNo = teacherPhoneNo;
        this.teacher_profile = teacher_profile;
        this.teacher_last_seen = teacher_last_seen;
        this.teacher_last_seen_permission = teacher_last_seen_permission;
        this.teacher_message_read_permission = teacher_message_read_permission;
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

    public String getTeacher_profile() {
        return teacher_profile;
    }

    public void setTeacher_profile(String teacher_profile) {
        this.teacher_profile = teacher_profile;
    }

    public String getTeacher_last_seen() {
        return teacher_last_seen;
    }

    public void setTeacher_last_seen(String teacher_last_seen) {
        this.teacher_last_seen = teacher_last_seen;
    }

    public String getTeacher_last_seen_permission() {
        return teacher_last_seen_permission;
    }

    public void setTeacher_last_seen_permission(String teacher_last_seen_permission) {
        this.teacher_last_seen_permission = teacher_last_seen_permission;
    }

    public String getTeacher_message_read_permission() {
        return teacher_message_read_permission;
    }

    public void setTeacher_message_read_permission(String teacher_message_read_permission) {
        this.teacher_message_read_permission = teacher_message_read_permission;
    }
}
