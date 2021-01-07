package com.schoolmanager.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ClassItem implements Parcelable {

    private int classId;
    private String  className;
    private List<SectionItem> sections;

    public ClassItem() {
    }

    public ClassItem(int classId, String className, List<SectionItem> sections) {
        this.classId = classId;
        this.className = className;
        this.sections = sections;
    }

    protected ClassItem(Parcel in) {
        classId = in.readInt();
        className = in.readString();
        sections = in.createTypedArrayList(SectionItem.CREATOR);
    }

    public static final Creator<ClassItem> CREATOR = new Creator<ClassItem>() {
        @Override
        public ClassItem createFromParcel(Parcel in) {
            return new ClassItem(in);
        }

        @Override
        public ClassItem[] newArray(int size) {
            return new ClassItem[size];
        }
    };

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<SectionItem> getSections() {
        return sections;
    }

    public void setSections(List<SectionItem> sections) {
        this.sections = sections;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(classId);
        dest.writeString(className);
        dest.writeTypedList(sections);
    }
}
