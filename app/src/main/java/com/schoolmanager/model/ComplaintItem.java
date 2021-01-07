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

    public ComplaintItem() {
    }

    public ComplaintItem(String chat_last_message_time, String chat_last_message, String chat_code, int chat_id, String chat_receiver_image, String chat_receiver_name, int chat_receiver_id, int chat_receiver_type) {
        this.chat_last_message_time = chat_last_message_time;
        this.chat_last_message = chat_last_message;
        this.chat_code = chat_code;
        this.chat_id = chat_id;
        this.chat_receiver_image = chat_receiver_image;
        this.chat_receiver_name = chat_receiver_name;
        this.chat_receiver_id = chat_receiver_id;
        this.chat_receiver_type = chat_receiver_type;
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
}
