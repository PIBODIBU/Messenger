package com.android.privatemessenger.data.model;

import android.content.Context;
import android.util.Log;

import com.android.privatemessenger.R;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Chat implements Serializable {
    private final String TAG = "Chat";

    public static final int TYPE_PRIVATE = 0;
    public static final int TYPE_PUBLIC = 1;

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

    @SerializedName("type")
    private int type;

    private int unreadCount;

    public Chat(String name, String createdAt, Message lastMessage, int participantsCount, List<User> participants, int type) {
        this.name = name;
        this.createdAt = createdAt;
        this.lastMessage = lastMessage;
        this.participantsCount = participantsCount;
        this.participants = participants;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getType() {
        return type;
    }

    public String getFormattedDate(Context context) {
        String dayMonth = "";
        String[] rusMonths = {
                "янв", "фев", "мар", "апр", "мая", "июн",
                "июл", "авг", "сен", "окт", "ноя", "дек"
        };

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date lastMessageDate = format.parse(getLastMessage().getCreatedAt());

            // Format last message date
            format = new SimpleDateFormat("dd.MM", Locale.getDefault());
            format.setTimeZone(TimeZone.getDefault());
            String messageDate = format.format(lastMessageDate);

            // Get device date
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat androidFormatter = new SimpleDateFormat("dd.MM", Locale.getDefault());
            androidFormatter.setTimeZone(TimeZone.getDefault());
            String androidDayMonth = androidFormatter.format(calendar.getTime());

            SimpleDateFormat yesterdayFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
            androidFormatter.setTimeZone(TimeZone.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterdayDate = yesterdayFormat.format(cal.getTime());

            Log.d(TAG, "getFormattedDate()-> yesterday: " + yesterdayDate);

            if (androidDayMonth.equals(messageDate)) {
                // Today
                return lastMessage.getFormattedDate();
            } else if (yesterdayDate.equals(messageDate)) {
                // Yesterday
                return context.getResources().getString(R.string.yesterday);
            }

            // Format date
            dayMonth = messageDate;
            String[] splittedDate = dayMonth.split("\\.");
            dayMonth = splittedDate[0] + " " + rusMonths[Integer.valueOf(splittedDate[1]) - 1];
        } catch (ParseException e) {
            Log.e(TAG, "getFormattedDate()-> ParseException", e);
            return "";
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getFormattedDate()-> ArrayIndexOutOfBoundsException", e);
            return "";
        } catch (NullPointerException e) {
            Log.e(TAG, "getFormattedDate()-> NullPointerException", e);
            return "";
        }

        return dayMonth;
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
