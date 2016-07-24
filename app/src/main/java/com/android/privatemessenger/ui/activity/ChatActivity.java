package com.android.privatemessenger.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.privatemessenger.R;
import com.android.privatemessenger.broadcast.IntentFilters;
import com.android.privatemessenger.broadcast.IntentKeys;
import com.google.firebase.iid.FirebaseInstanceId;

import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity {

    private final String TAG = ChatActivity.this.getClass().getSimpleName();

    private BroadcastReceiver messageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        Log.d(TAG, "Token: " + FirebaseInstanceId.getInstance().getToken());

        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, intent.getExtras().getString(IntentKeys.TEXT));
            }
        };
        registerReceiver(messageReceiver, new IntentFilter(IntentFilters.NEW_MESSAGE));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(messageReceiver);
        super.onDestroy();
    }
}
