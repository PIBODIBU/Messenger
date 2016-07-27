package com.android.privatemessenger.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("error")
    private ErrorResponse errorResponse;

    @SerializedName("user")
    private User user;

    public LoginResponse(ErrorResponse errorResponse, User user) {
        this.errorResponse = errorResponse;
        this.user = user;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public User getUser() {
        return user;
    }
}
