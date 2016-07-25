package com.android.privatemessenger.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {
    @SerializedName("user_id")
    private int id;

    @SerializedName("token")
    private String token;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("gcm_registration_id")
    private String fcmId;

    @SerializedName("created_at")
    private String createDateTime;

    public User(int id, String token, String name, String phone, String email, String fcmId, String createDateTime) {
        this.id = id;
        this.token = token;
        this.name = name;
        this.phone = phone;
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

    public String getPhone() {
        return phone;
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
