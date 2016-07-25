package com.android.privatemessenger.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SendMessageResponse implements Serializable {
    @SerializedName("error")
    private ErrorResponse errorResponse;

    @SerializedName("message")
    private Message message;

    public SendMessageResponse(ErrorResponse errorResponse, Message message) {
        this.errorResponse = errorResponse;
        this.message = message;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public Message getMessage() {
        return message;
    }
}
