package com.schoolmanager.events;

public class EventDeleteMessage {
    String messgae_id = "";

    public EventDeleteMessage(String messgae_id) {
        this.messgae_id = messgae_id;
    }

    public String getMessgae_id() {
        return messgae_id;
    }

    public void setMessgae_id(String messgae_id) {
        this.messgae_id = messgae_id;
    }
}
