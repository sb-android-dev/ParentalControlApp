package com.schoolmanager.model;

public class StudentItem {

    private int studentId;
    private String studentName;
    private String className;
    private String sectionName;

    public StudentItem() {
    }

    public StudentItem(int studentId, String studentName, String className, String sectionName) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.sectionName = sectionName;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
}
