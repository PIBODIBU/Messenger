package com.android.privatemessenger.data.model;

import com.google.gson.annotations.SerializedName;

public class ErrorResponse {

    @SerializedName("error")
    private boolean error;

    @SerializedName("error_msg")
    private String errorMessage;

    public ErrorResponse(boolean error, String errorMessage) {
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public boolean isError() {
        return error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
