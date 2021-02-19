package com.schoolmanager.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlacesItem {


    @Expose
    @SerializedName("place_name")
    private String place_name;
    @Expose
    @SerializedName("place_id")
    private int place_id;

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public int getPlace_id() {
        return place_id;
    }

    public void setPlace_id(int place_id) {
        this.place_id = place_id;
    }
}
