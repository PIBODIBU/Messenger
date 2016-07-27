package com.android.privatemessenger.firebase;

import android.util.Log;

import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        SharedPrefUtils.getInstance(this).setToken(refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        RetrofitAPI.getInstance().updateFCMId(
                SharedPrefUtils.getInstance(this).getUser().getToken(),
                token
        ).enqueue(new Callback<ErrorResponse>() {
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
}