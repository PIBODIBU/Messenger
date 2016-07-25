package com.android.privatemessenger.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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


    @BindView(R.id.til_name)
    public TextInputLayout TILName;

    @BindView(R.id.til_phone)
    public TextInputLayout TILPhone;

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

                String name = "";
                final ProgressDialog progressDialog;

                if (TILName.getEditText() != null)
                    name = TILName.getEditText().getText().toString();

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
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_out_right, R.anim.pull_in_left);
    }

    @OnClick(R.id.btn_login)
    public void login() {
        if (!isInputValid()) {
            return;
        }

        String phone = "";

        if (TILPhone.getEditText() != null) {
            phone = TILPhone.getEditText().getText().toString();
        }

        Digits.authenticate(new DigitsAuthConfig.Builder()
                .withAuthCallBack(authCallback)
                .withPhoneNumber(phone)
                .build());
    }

    private boolean isInputValid() {
        if (TILPhone.getEditText() == null || TILName.getEditText() == null) {
            return false;
        }

        String phone = TILPhone.getEditText().getText().toString();
        String name = TILName.getEditText().getText().toString();

        if (!phone.startsWith("+")) {
            TILPhone.setError(getResources().getString(R.string.error_must_start_with));
            return false;
        }

        return true;
    }

    public AuthCallback getAuthCallback() {
        return authCallback;
    }
}
