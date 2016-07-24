package com.android.privatemessenger.data.api;

import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.model.ErrorResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface IAPIService {
    @GET("my/chats")
    Call<List<Chat>> getMyChats();

    @POST("my/gcm/id/update")
    Call<ErrorResponse> updateFCMId(@Field("gcm_registration_id") String id);
}
