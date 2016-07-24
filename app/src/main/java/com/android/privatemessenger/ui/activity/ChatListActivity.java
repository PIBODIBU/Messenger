package com.android.privatemessenger.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.digits.sdk.android.Digits;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListActivity extends BaseNavDrawerActivity {

    public final String TAG = ChatListActivity.this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        getDrawer();

        RetrofitAPI.getInstance().getMyChats().enqueue(new Callback<List<Chat>>() {
            @Override
            public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                Log.d(TAG, response.toString());
            }

            @Override
            public void onFailure(Call<List<Chat>> call, Throwable t) {
                Log.e(TAG, "Error occurred during my chat list fetching", t);
            }
        });
    }

    @OnClick(R.id.btn_logout)
    public void logout() {
        Digits.getSessionManager().clearActiveSession();
        startActivity(new Intent(ChatListActivity.this, LoginActivity.class));
        finish();
    }


}
