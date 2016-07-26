package com.android.privatemessenger.utils;

import java.util.Calendar;

public class TimeUtils {
    public static String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        return String.valueOf(c.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(c.get(Calendar.MINUTE));
    }
}
