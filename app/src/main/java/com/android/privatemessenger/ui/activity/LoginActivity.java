package com.android.privatemessenger.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.LoginResponse;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthConfig;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.common.SafeToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    public final String TAG = LoginActivity.this.getClass().getSimpleName();

    @BindString(R.string.twitter_key)
    public String TWITTER_KEY;

    @BindString(R.string.twitter_secret)
    public String TWITTER_SECRET;

    @BindView(R.id.et_name)
    public AppCompatEditText APETName;

    @BindView(R.id.et_phone)
    public AppCompatEditText APETPhone;

    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    private AuthCallback authCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits());

        setupToolbar();
        createCallbacks();
    }

    private void createCallbacks() {
        authCallback = new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                Log.i(TAG, "success()-> Sign in successful");

                String name = APETName.getText().toString();

                final ProgressDialog progressDialog;
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage(getResources().getString(R.string.dialog_loading));
                progressDialog.show();

                RetrofitAPI.getInstance().login(name, phoneNumber).enqueue(new Callback<LoginResponse>() {
                    private void handleError() {
                        progressDialog.cancel();
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.toast_login_fail), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response.body() == null) {
                            handleError();
                            return;
                        }

                        SharedPrefUtils.getInstance(LoginActivity.this).setUser(response.body().getUser());
                        progressDialog.cancel();
                        startActivity(new Intent(LoginActivity.this, ChatListActivity.class));
                        finish();
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Log.e(TAG, "onFailure()-> Error occurred during login", t);

                        handleError();
                    }
                });
            }

            @Override
            public void failure(DigitsException error) {
                Log.e(TAG, "failure()-> Sign in failure");
            }
        };
    }

    private void setupToolbar() {
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);

        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @OnClick(R.id.btn_login)
    public void login() {
        if (!isInputValid()) {
            return;
        }

        String phone = APETPhone.getText().toString();

        Digits.authenticate(new DigitsAuthConfig.Builder()
                .withAuthCallBack(authCallback)
                .withThemeResId(R.style.CustomDigitsTheme)
                .withPhoneNumber(phone)
                .build());
    }

    private boolean isInputValid() {
        String phone = APETPhone.getText().toString();
        String name = APETName.getText().toString();

        if (!phone.startsWith("+")) {
            Toast.makeText(LoginActivity.this, getResources().getString(R.string.toast_must_start_from), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (name.equals("")) {
            Toast.makeText(LoginActivity.this, getResources().getString(R.string.toast_enter_name), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public AuthCallback getAuthCallback() {
        return authCallback;
    }
}
