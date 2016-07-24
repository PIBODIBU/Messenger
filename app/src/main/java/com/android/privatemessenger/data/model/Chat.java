package com.android.privatemessenger.data.model;

import com.google.gson.annotations.SerializedName;

public class Chat {
    @SerializedName("chat_room_id")
    private int id;

    @SerializedName("name")
    private String name;

    public Chat(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
