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

    private boolean selected;

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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setCreateDateTime(String createDateTime) {
        this.createDateTime = createDateTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFcmId(String fcmId) {
        this.fcmId = fcmId;
    }
}
