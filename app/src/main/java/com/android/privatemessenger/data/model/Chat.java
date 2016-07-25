package com.android.privatemessenger.data.model;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Chat {
    @SerializedName("chat_room_id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("last_message")
    private Message lastMessage;

    public Chat(int id, String name, String createdAt, Message lastMessage) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.lastMessage = lastMessage;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public String getFormattedDate() {
        String newDate = "";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        try {
            Date oldDate = format.parse(getCreatedAt());
            format = new SimpleDateFormat("MM.dd HH:mm", Locale.getDefault());
            newDate = format.format(oldDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return newDate;
    }
}
