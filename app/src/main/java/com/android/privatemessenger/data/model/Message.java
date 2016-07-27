package com.android.privatemessenger.data.model;

import android.content.Context;
import android.util.Log;

import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Message implements Serializable {
    private static final String TAG = "Message";

    public static final int TYPE_MY = 1;
    public static final int TYPE_FOREIGN = 2;

    public static final int STATUS_SENDING = 1;
    public static final int STATUS_ERROR = 2;
    public static final int STATUS_SENT = 3;

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

    @SerializedName("sender")
    private User sender;

    private int type = -1;

    private int sendStatus = STATUS_SENT;

    public Message(int messageId, int chatRoomId, int userId, String message, String createdAt, User sender) {
        this.messageId = messageId;
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.message = message;
        this.createdAt = createdAt;
        this.sender = sender;
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

    public User getSender() {
        return sender;
    }

    public int getType(Context context) {
        Log.d("Message", "getType()-> " +
                "\nfrom SharedPrefs: " + SharedPrefUtils.getInstance(context).getUser().getId() +
                "\nuserId: " + userId);

        return userId == SharedPrefUtils.getInstance(context).getUser().getId() ? TYPE_MY : TYPE_FOREIGN;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setSendStatus(int sendStatus) {
        Log.d(TAG, "setSendStatus()-> Status: " + sendStatus);
        this.sendStatus = sendStatus;
    }

    public String getFormattedDate() {
        String newDate = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (getCreatedAt() == null) {
            return "";
        }

        try {
            Date oldDate = format.parse(getCreatedAt());

            format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            format.setTimeZone(TimeZone.getDefault());

            newDate = format.format(oldDate);
        } catch (ParseException e) {
            Log.e(TAG, "getFormattedDate()-> ParseException", e);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getFormattedDate()-> ArrayIndexOutOfBoundsException", e);
        }

        return newDate;
    }
}
