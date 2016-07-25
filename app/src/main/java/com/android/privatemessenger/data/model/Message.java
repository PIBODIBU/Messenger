package com.android.privatemessenger.data.model;

import com.google.gson.annotations.SerializedName;

public class Message {

    @SerializedName("message_id")
    private int messageId;

    @SerializedName("chat_room_id")
    private int chatRoomId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("message")
    private String message;

    @SerializedName("created_at")
    private String createdAt;

    public Message(int messageId, int chatRoomId, int userId, String message, String createdAt) {
        this.messageId = messageId;
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getChatRoomId() {
        return chatRoomId;
    }

    public int getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
