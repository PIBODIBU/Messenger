package com.android.privatemessenger.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.privatemessenger.R;
import com.google.firebase.iid.FirebaseInstanceId;

import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity {

    private final String TAG = ChatActivity.this.getClass().getSimpleName();

    private MessageReceiver messageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        Log.d(TAG, "Token: " + FirebaseInstanceId.getInstance().getToken());

        messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver, new IntentFilter("com.android.privatemessenger.NEW_MESSAGE"));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(messageReceiver);
        super.onDestroy();
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, intent.getExtras().getString("text"));
        }
    }
}
