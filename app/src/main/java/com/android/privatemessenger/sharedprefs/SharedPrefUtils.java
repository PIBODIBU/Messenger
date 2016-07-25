package com.android.privatemessenger.sharedprefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.privatemessenger.data.model.User;

public class SharedPrefUtils {
    private static SharedPrefUtils sharedPrefUtils = null;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS_NAME = "com.android.privatemessenger";

    public static SharedPrefUtils getInstance(Context context) {
        if (sharedPrefUtils == null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            return new SharedPrefUtils(sharedPreferences);
        } else {
            return sharedPrefUtils;
        }
    }

    public SharedPrefUtils(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    public User getUser() {
        return new User(
                sharedPreferences.getInt(SharedPrefKeys.ID, -1),
                sharedPreferences.getString(SharedPrefKeys.TOKEN, ""),
                sharedPreferences.getString(SharedPrefKeys.NAME, ""),
                sharedPreferences.getString(SharedPrefKeys.PHONE, ""),
                sharedPreferences.getString(SharedPrefKeys.EMAIL, ""),
                sharedPreferences.getString(SharedPrefKeys.FCM_ID, ""),
                sharedPreferences.getString(SharedPrefKeys.CREATE_DATE_TIME, "")
        );
    }

    public void setUser(User user) {
        sharedPreferences.edit()
                .putInt(SharedPrefKeys.ID, user.getId())
                .putString(SharedPrefKeys.TOKEN, user.getToken())
                .putString(SharedPrefKeys.NAME, user.getName())
                .putString(SharedPrefKeys.PHONE, user.getPhone())
                .putString(SharedPrefKeys.EMAIL, user.getEmail())
                .putString(SharedPrefKeys.FCM_ID, user.getFcmId())
                .putString(SharedPrefKeys.CREATE_DATE_TIME, user.getCreateDateTime())
                .apply();
    }

    public void setId(int id) {
        sharedPreferences.edit().putInt(SharedPrefKeys.ID, id).apply();
    }

    public void setToken(String token) {
        sharedPreferences.edit().putString(SharedPrefKeys.TOKEN, token).apply();
    }

    public void setName(String name) {
        sharedPreferences.edit().putString(SharedPrefKeys.NAME, name).apply();
    }

    public void setPhone(String phone) {
        sharedPreferences.edit().putString(SharedPrefKeys.PHONE, phone).apply();
    }

    public void setEmail(String email) {
        sharedPreferences.edit().putString(SharedPrefKeys.EMAIL, email).apply();
    }

    public void setFcmId(String fcmId) {
        sharedPreferences.edit().putString(SharedPrefKeys.FCM_ID, fcmId).apply();
    }

    public void setCreateDateTime(String createDateTime) {
        sharedPreferences.edit().putString(SharedPrefKeys.CREATE_DATE_TIME, createDateTime).apply();
    }
}
