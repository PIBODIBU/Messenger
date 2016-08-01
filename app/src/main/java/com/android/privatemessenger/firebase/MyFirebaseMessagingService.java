package com.android.privatemessenger.firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.android.privatemessenger.R;
import com.android.privatemessenger.application.ActivityWatcher;
import com.android.privatemessenger.broadcast.BroadcastKeys;
import com.android.privatemessenger.broadcast.IntentFilters;
import com.android.privatemessenger.data.realm.RealmDB;
import com.android.privatemessenger.data.realm.model.UnreadMessage;
import com.android.privatemessenger.ui.activity.ChatListActivity;
import com.android.privatemessenger.utils.IntentKeys;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        for (Map.Entry entry : remoteMessage.getData().entrySet()) {
            Log.d(TAG, "onMessageReceived()-> " + entry.getKey() + " --- " + entry.getValue());
        }

        try {
            JSONObject jsonMessage = new JSONObject(remoteMessage.getData().get(BroadcastKeys.JSON_OBJECT_MESSAGE));
            JSONObject jsonSender = jsonMessage.getJSONObject(BroadcastKeys.JSON_OBJECT_SENDER);

            sendBroadcast(new Intent(IntentFilters.NEW_MESSAGE)
                    .putExtra(IntentKeys.MESSAGE_ID, jsonMessage.getInt(BroadcastKeys.MESSAGE_ID))
                    .putExtra(IntentKeys.MESSAGE, jsonMessage.getString(BroadcastKeys.MESSAGE))
                    .putExtra(IntentKeys.CHAT_ROOM_ID, jsonMessage.getInt(BroadcastKeys.CHAT_ROOM_ID))
                    .putExtra(IntentKeys.CREATED_AT, jsonMessage.getString(BroadcastKeys.CREATED_AT))
                    .putExtra(IntentKeys.MESSAGE_ID, jsonMessage.getInt(BroadcastKeys.MESSAGE_ID))
                    .putExtra(IntentKeys.SENDER_ID, jsonSender.getInt(BroadcastKeys.SENDER_ID))
                    .putExtra(IntentKeys.SENDER_NAME, jsonSender.getString(BroadcastKeys.SENDER_NAME))
                    .putExtra(IntentKeys.SENDER_PHONE, jsonSender.getString(BroadcastKeys.SENDER_PHONE))
                    .putExtra(IntentKeys.SENDER_EMAIl, jsonSender.getString(BroadcastKeys.SENDER_EMAIL))
            );

            if (!ActivityWatcher.isChatActivityShowing() && ActivityWatcher.getCurrentChatId() != jsonMessage.getInt(BroadcastKeys.CHAT_ROOM_ID)) {
                sendNotification(jsonSender.getString(BroadcastKeys.SENDER_NAME), jsonMessage.getString(BroadcastKeys.MESSAGE));
            }

            addUnreadToDB(jsonMessage.getInt(BroadcastKeys.CHAT_ROOM_ID));
        } catch (JSONException e) {
            Log.e(TAG, "onMessageReceived()-> ", e);
        } catch (Exception ex) {
            Log.e(TAG, "onMessageReceived()-> ", ex);
        }
    }

    private void addUnreadToDB(int chatId) {
        Realm realm = Realm.getInstance(new RealmConfiguration.Builder(getApplicationContext())
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build());

        UnreadMessage oldModel = realm.where(UnreadMessage.class).equalTo("chatId", chatId).findFirst();
        int unreadCount = oldModel == null ? 1 : oldModel.getUnreadCount() + 1;
        Log.d(TAG, "addUnreadToDB()-> unreadCount: " + unreadCount);

        UnreadMessage unreadMessage = new UnreadMessage(chatId, unreadCount);
        realm.beginTransaction();
        realm.insertOrUpdate(unreadMessage);
        realm.commitTransaction();
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String title, String content) {
        Intent intent = new Intent(this, ChatListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }
}