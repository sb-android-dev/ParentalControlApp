package com.schoolmanager.model;

public class ChatMessageModal {

    private String message_id = "";
    private String message_time = "";
    private String message_text = "";
    private int message_type;
    private String message_file_url = "";
    private String message_sender_id = "";
    private String message_sender_type = "";
    private String message_receiver_type = "";
    private String message_receiver_id = "";

    public ChatMessageModal(
            String message_id,
            String message_time,
            String message_text,
            int message_type,
            String message_file_url,
            String message_sender_id,
            String message_receiver_id,
            String message_sender_type,
            String message_receiver_type) {

        this.message_id = message_id;
        this.message_time = message_time;
        this.message_text = message_text;
        this.message_type = message_type;
        this.message_file_url = message_file_url;
        this.message_sender_id = message_sender_id;
        this.message_receiver_id = message_receiver_id;
        this.message_sender_type = message_sender_type;
        this.message_receiver_type = message_receiver_type;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage_time() {
        return message_time;
    }

    public void setMessage_time(String message_time) {
        this.message_time = message_time;
    }

    public String getMessage_text() {
        return message_text;
    }

    public void setMessage_text(String message_text) {
        this.message_text = message_text;
    }

    public int getMessage_type() {
        return message_type;
    }

    public void setMessage_type(int message_type) {
        this.message_type = message_type;
    }

    public String getMessage_file_url() {
        return message_file_url;
    }

    public void setMessage_file_url(String message_file_url) {
        this.message_file_url = message_file_url;
    }

    public String getMessage_sender_id() {
        return message_sender_id;
    }

    public void setMessage_sender_id(String message_sender_id) {
        this.message_sender_id = message_sender_id;
    }

    public String getMessage_receiver_id() {
        return message_receiver_id;
    }

    public void setMessage_receiver_id(String message_receiver_id) {
        this.message_receiver_id = message_receiver_id;
    }

    public String getMessage_sender_type() {
        return message_sender_type;
    }

    public void setMessage_sender_type(String message_sender_type) {
        this.message_sender_type = message_sender_type;
    }

    public String getMessage_receiver_type() {
        return message_receiver_type;
    }

    public void setMessage_receiver_type(String message_receiver_type) {
        this.message_receiver_type = message_receiver_type;
    }
}
