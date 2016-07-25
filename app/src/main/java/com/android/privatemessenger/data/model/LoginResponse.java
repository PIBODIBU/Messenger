package com.android.privatemessenger.data.model;

public class LoginResponse {
    private ErrorResponse errorResponse;

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
