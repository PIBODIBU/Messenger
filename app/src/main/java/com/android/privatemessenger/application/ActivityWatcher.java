package com.android.privatemessenger.application;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

public class ActivityWatcher extends MultiDexApplication {
    private static final String TAG = "ActivityWatcher";

    private static boolean isChatActivityShowing = false;
    private static int currentChatId = -1;

    public static boolean isChatActivityShowing() {
        return isChatActivityShowing;
    }

    public static void setChatActivityShowing(boolean chatActivityShowing) {
        Log.d(TAG, "setChatActivityShowing()-> chatActivityShowing: " + chatActivityShowing);
        isChatActivityShowing = chatActivityShowing;
    }

    public static int getCurrentChatId() {
        return currentChatId;
    }

    public static void dropCurrentChatId() {
        ActivityWatcher.currentChatId = -1;
    }

    public static void setCurrentChatId(int currentChatId) {
        Log.d(TAG, "setCurrentChatId()-> currentChatId: " + currentChatId);
        ActivityWatcher.currentChatId = currentChatId;
    }
}
