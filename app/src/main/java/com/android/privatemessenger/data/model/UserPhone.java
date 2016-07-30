package com.android.privatemessenger.data.model;

public class UserPhone {
    private String phone;

    public UserPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return phone;
    }
}
