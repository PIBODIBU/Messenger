package com.android.privatemessenger.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("user_id")
    private int id;

    @SerializedName("token")
    private String token;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("gcm_registration_id")
    private String fcmId;

    @SerializedName("created_at")
    private String createDateTime;

    public User(int id, String token, String name, String email, String fcmId, String createDateTime) {
        this.id = id;
        this.token = token;
        this.name = name;
        this.email = email;
        this.fcmId = fcmId;
        this.createDateTime = createDateTime;
    }

    public int getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getFcmId() {
        return fcmId;
    }

    public String getCreateDateTime() {
        return createDateTime;
    }
}
