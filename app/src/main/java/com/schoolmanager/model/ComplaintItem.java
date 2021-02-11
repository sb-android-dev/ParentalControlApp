package com.schoolmanager.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ComplaintItem {

    @Expose
    @SerializedName("chat_last_message_time")
    private String chat_last_message_time;
    @Expose
    @SerializedName("chat_last_message")
    private String chat_last_message;
    @Expose
    @SerializedName("chat_code")
    private String chat_code;
    @Expose
    @SerializedName("chat_id")
    private int chat_id;
    @Expose
    @SerializedName("chat_receiver_image")
    private String chat_receiver_image;
    @Expose
    @SerializedName("chat_receiver_name")
    private String chat_receiver_name;
    @Expose
    @SerializedName("chat_receiver_id")
    private int chat_receiver_id;
    @Expose
    @SerializedName("chat_receiver_type")
    private int chat_receiver_type;
    @Expose
    @SerializedName("chat_unread_count")
    private int chat_unread_count;
    @Expose
    @SerializedName("chat_receiver_last_seen")
    private int chat_receiver_last_seen;
    @Expose
    @SerializedName("chat_read_permission")
    private int chat_read_permission;
    @Expose
    @SerializedName("chat_last_seen_permission")
    private int chat_last_seen_permission;

    public ComplaintItem() {
    }

    public ComplaintItem(String chat_last_message_time, String chat_last_message, String chat_code, int chat_id, String chat_receiver_image, String chat_receiver_name, int chat_receiver_id, int chat_receiver_type,
                         int chat_read_permission,
                         int chat_last_seen_permission) {
        this.chat_last_message_time = chat_last_message_time;
        this.chat_last_message = chat_last_message;
        this.chat_code = chat_code;
        this.chat_id = chat_id;
        this.chat_receiver_image = chat_receiver_image;
        this.chat_receiver_name = chat_receiver_name;
        this.chat_receiver_id = chat_receiver_id;
        this.chat_receiver_type = chat_receiver_type;
        this.chat_read_permission = chat_read_permission;
        this.chat_last_seen_permission = chat_last_seen_permission;
    }

    public String getChat_last_message_time() {
        return chat_last_message_time;
    }

    public void setChat_last_message_time(String chat_last_message_time) {
        this.chat_last_message_time = chat_last_message_time;
    }

    public String getChat_last_message() {
        return chat_last_message;
    }

    public void setChat_last_message(String chat_last_message) {
        this.chat_last_message = chat_last_message;
    }

    public String getChat_code() {
        return chat_code;
    }

    public void setChat_code(String chat_code) {
        this.chat_code = chat_code;
    }

    public int getChat_id() {
        return chat_id;
    }

    public void setChat_id(int chat_id) {
        this.chat_id = chat_id;
    }

    public String getChat_receiver_image() {
        return chat_receiver_image;
    }

    public void setChat_receiver_image(String chat_receiver_image) {
        this.chat_receiver_image = chat_receiver_image;
    }

    public String getChat_receiver_name() {
        return chat_receiver_name;
    }

    public void setChat_receiver_name(String chat_receiver_name) {
        this.chat_receiver_name = chat_receiver_name;
    }

    public int getChat_receiver_id() {
        return chat_receiver_id;
    }

    public void setChat_receiver_id(int chat_receiver_id) {
        this.chat_receiver_id = chat_receiver_id;
    }

    public int getChat_receiver_type() {
        return chat_receiver_type;
    }

    public void setChat_receiver_type(int chat_receiver_type) {
        this.chat_receiver_type = chat_receiver_type;
    }

    public int getChat_unread_count() {
        return chat_unread_count;
    }

    public void setChat_unread_count(int chat_unread_count) {
        this.chat_unread_count = chat_unread_count;
    }

    public int getChat_receiver_last_seen() {
        return chat_receiver_last_seen;
    }

    public void setChat_receiver_last_seen(int chat_receiver_last_seen) {
        this.chat_receiver_last_seen = chat_receiver_last_seen;
    }

    public int getChat_read_permission() {
        return chat_read_permission;
    }

    public void setChat_read_permission(int chat_read_permission) {
        this.chat_read_permission = chat_read_permission;
    }

    public int getChat_last_seen_permission() {
        return chat_last_seen_permission;
    }

    public void setChat_last_seen_permission(int chat_last_seen_permission) {
        this.chat_last_seen_permission = chat_last_seen_permission;
    }
}
