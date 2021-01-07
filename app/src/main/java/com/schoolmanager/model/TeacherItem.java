package com.schoolmanager.model;

public class TeacherItem {

    private int teacherId;
    private String teacherName;
    private String subjectName;
    private String teacherPhoneNo;

    public TeacherItem() {
    }

    public TeacherItem(int teacherId, String teacherName, String subjectName, String teacherPhoneNo) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.subjectName = subjectName;
        this.teacherPhoneNo = teacherPhoneNo;
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
}
