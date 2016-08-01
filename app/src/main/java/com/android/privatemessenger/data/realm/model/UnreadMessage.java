package com.android.privatemessenger.data.realm.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UnreadMessage extends RealmObject {
    @PrimaryKey
    private int id;

    private int chatId;
    private int unreadCount;

    public UnreadMessage() {
    }

    public UnreadMessage(int chatId, int unreadCount) {
        this.chatId = chatId;
        this.unreadCount = unreadCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
