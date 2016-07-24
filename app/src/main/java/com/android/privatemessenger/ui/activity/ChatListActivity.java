package com.android.privatemessenger.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.digits.sdk.android.Digits;
import com.google.firebase.iid.FirebaseInstanceId;

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
        setContentView(R.layout.activity_chat_list);

        ButterKnife.bind(this);
        getDrawer();

        updateGCMId();

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

    private void updateGCMId() {
        RetrofitAPI.getInstance().updateFCMId(FirebaseInstanceId.getInstance().getToken()).enqueue(new Callback<ErrorResponse>() {
            @Override
            public void onResponse(Call<ErrorResponse> call, Response<ErrorResponse> response) {
                if (response.body() != null) {
                    if (response.body().isError()) {
                        Log.e(TAG, "onFailure()-> Error during id update");
                    } else {
                        Log.i(TAG, "onResponse()-> GCM id updated successfully");
                    }
                }
            }

            @Override
            public void onFailure(Call<ErrorResponse> call, Throwable t) {
                Log.e(TAG, "onFailure()-> Error during id update", t);
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
