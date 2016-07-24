package com.android.privatemessenger.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.privatemessenger.R;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity {

    public final String TAG = LoginActivity.this.getClass().getSimpleName();

    @BindString(R.string.twitter_key)
    public String TWITTER_KEY;

    @BindString(R.string.twitter_secrey)
    public String TWITTER_SECRET;

    @BindView(R.id.auth_button)
    public DigitsAuthButton digitsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits());

        digitsButton.setAuthTheme(R.style.CustomDigitsTheme);
        digitsButton.setCallback(new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                // TODO: associate the session userID with your user model
                startActivity(new Intent(LoginActivity.this, ChatListActivity.class));
                finish();
            }

            @Override
            public void failure(DigitsException exception) {
                Log.d("Digits", "Sign in with Digits failure", exception);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

}
