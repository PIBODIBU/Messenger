package com.android.privatemessenger.data.realm;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmDB {
    private static Realm realm = null;

    public static Realm getInstance(Context context) {
        if (realm == null) {
            realm = Realm.getInstance(new RealmConfiguration.Builder(context)
                    .name(Realm.DEFAULT_REALM_NAME)
                    .schemaVersion(0)
                    .deleteRealmIfMigrationNeeded()
                    .build());
        }

        return realm;
    }
}
