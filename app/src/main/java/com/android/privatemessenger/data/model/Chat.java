package com.android.privatemessenger.data.model;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Chat implements Serializable {
    private final String TAG = "Chat";

    @SerializedName("chat_room_id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("last_message")
    private Message lastMessage;

    @SerializedName("participants_count")
    private int participantsCount;

    @SerializedName("participants")
    private List<User> participants;

    private int unreadCount;

    public Chat(String name, String createdAt, Message lastMessage, int participantsCount, List<User> participants) {
        this.name = name;
        this.createdAt = createdAt;
        this.lastMessage = lastMessage;
        this.participantsCount = participantsCount;
        this.participants = participants;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public int getParticipantsCount() {
        return participantsCount;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public String getFormattedDate() {
        String newDate = "";
        String[] rusMonths = {
                "янв", "фев", "мар", "апр", "мая", "июн",
                "июл", "авг", "сен", "окт", "ноя", "дек"
        };

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (getCreatedAt() == null) {
            return "";
        }

        try {
            Date oldDate = format.parse(getCreatedAt());

            format = new SimpleDateFormat("dd.MM", Locale.getDefault());
            format.setTimeZone(TimeZone.getDefault());

            newDate = format.format(oldDate);

            Log.d(TAG, "getFormattedDate()-> Date: " + newDate);

            String[] splittedDate = newDate.split("\\.");
            newDate = splittedDate[0] + " " + rusMonths[Integer.valueOf(splittedDate[1]) - 1];
        } catch (ParseException e) {
            Log.e(TAG, "getFormattedDate()-> ParseException", e);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getFormattedDate()-> ArrayIndexOutOfBoundsException", e);
        }

        return newDate;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
