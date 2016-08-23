package com.android.privatemessenger.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Contact implements Serializable {

    @SerializedName("contact_id")
    private int id;

    @SerializedName("owner_id")
    private int ownerId;

    @SerializedName("phone")
    private String phone;

    @SerializedName("name")
    private String name;

    @SerializedName("is_registered")
    private String isRegistered;


    public Contact(int id, int ownerId, String phone, String name, String isRegistered) {
        this.id = id;
        this.ownerId = ownerId;
        this.phone = phone;
        this.name = name;
        this.isRegistered = isRegistered;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public boolean isRegistered() {
        return isRegistered.equals("1");
    }
}
