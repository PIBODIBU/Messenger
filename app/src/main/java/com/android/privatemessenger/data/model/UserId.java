package com.android.privatemessenger.data.model;

import com.google.gson.annotations.SerializedName;

public class UserId {
    @SerializedName("user_id")
    private int userId;

    public UserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }
}
