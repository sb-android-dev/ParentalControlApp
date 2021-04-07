package com.schoolmanager.events;

public class EventLocationStatusChanged {
    String driver_id = "";

    public EventLocationStatusChanged(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }
}
