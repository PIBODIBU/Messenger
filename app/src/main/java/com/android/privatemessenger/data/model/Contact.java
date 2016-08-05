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

    public Contact(int id, int ownerId, String phone, String name) {
        this.id = id;
        this.ownerId = ownerId;
        this.phone = phone;
        this.name = name;
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
}
