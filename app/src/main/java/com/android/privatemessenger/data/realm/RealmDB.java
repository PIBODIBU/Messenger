package com.android.privatemessenger.data.realm;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmDB {
    public static Realm getDefault(Context context) {
        return Realm.getInstance(new RealmConfiguration.Builder(context)
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build());
    }
}