package com.android.privatemessenger.data.model;

import com.google.gson.annotations.SerializedName;

public class ChatCreateResponse {
    @SerializedName("error")
    private ErrorResponse errorResponse;

    @SerializedName("chat")
    private Chat chat;

    public ChatCreateResponse(ErrorResponse errorResponse, Chat chat) {
        this.errorResponse = errorResponse;
        this.chat = chat;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public Chat getChat() {
        return chat;
    }
}
