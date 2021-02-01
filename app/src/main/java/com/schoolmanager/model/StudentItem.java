package com.schoolmanager.model;

public class StudentItem {

    private int studentId;
    private String studentName;
    private String className;
    private String sectionName;
    private int parentId;
    private String parentName;
    private String parentImage;

    public StudentItem() {
    }

    public StudentItem(int studentId, String studentName, String className, String sectionName,
                       int parentId, String parentName, String parentImage) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.sectionName = sectionName;
        this.parentId = parentId;
        this.parentName = parentName;
        this.parentImage = parentImage;
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

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentImage() {
        return parentImage;
    }

    public void setParentImage(String parentImage) {
        this.parentImage = parentImage;
    }
}
