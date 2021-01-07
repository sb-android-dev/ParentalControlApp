package com.schoolmanager.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SectionItem implements Parcelable {

    private int sectionId;
    private String sectionName;

    public SectionItem() {
    }

    public SectionItem(int sectionId, String sectionName) {
        this.sectionId = sectionId;
        this.sectionName = sectionName;
    }

    protected SectionItem(Parcel in) {
        sectionId = in.readInt();
        sectionName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sectionId);
        dest.writeString(sectionName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SectionItem> CREATOR = new Creator<SectionItem>() {
        @Override
        public SectionItem createFromParcel(Parcel in) {
            return new SectionItem(in);
        }

        @Override
        public SectionItem[] newArray(int size) {
            return new SectionItem[size];
        }
    };

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
}
