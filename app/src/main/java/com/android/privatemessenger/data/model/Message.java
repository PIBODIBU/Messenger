package com.android.privatemessenger.data.model;

public class Message {

    private int id;

    private String text;

    private String time;

    private int senderId;

    public Message(int id, String text, String time, int senderId) {
        this.id = id;
        this.text = text;
        this.time = time;
        this.senderId = senderId;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public int getSenderId() {
        return senderId;
    }
}
