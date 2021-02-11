package com.schoolmanager.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public  class BroadCastMessageItem {

    @Expose
    @SerializedName("broadcast_time")
    private String broadcast_time;
    @Expose
    @SerializedName("broadcast_message")
    private String broadcast_message;
    @Expose
    @SerializedName("broadcast_id")
    private int broadcast_id;

    public String getBroadcast_time() {
        return broadcast_time;
    }

    public void setBroadcast_time(String broadcast_time) {
        this.broadcast_time = broadcast_time;
    }

    public String getBroadcast_message() {
        return broadcast_message;
    }

    public void setBroadcast_message(String broadcast_message) {
        this.broadcast_message = broadcast_message;
    }

    public int getBroadcast_id() {
        return broadcast_id;
    }

    public void setBroadcast_id(int broadcast_id) {
        this.broadcast_id = broadcast_id;
    }
}
