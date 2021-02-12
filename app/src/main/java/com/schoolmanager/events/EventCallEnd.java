package com.schoolmanager.events;

public class EventCallEnd {
    String call_id;
    String  call_token;

    public EventCallEnd(String call_id, String call_token) {
        this.call_id = call_id;
        this.call_token = call_token;
    }

    public String getCall_id() {
        return call_id;
    }

    public String getCall_token() {
        return call_token;
    }
}
