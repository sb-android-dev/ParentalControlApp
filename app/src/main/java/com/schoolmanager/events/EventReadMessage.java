package com.schoolmanager.events;

public class EventReadMessage  {
    String message_id = "";

    public EventReadMessage(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }
}
