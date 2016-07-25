package com.android.privatemessenger.data.api;

import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.android.privatemessenger.data.model.LoginResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IAPIService {
    @FormUrlEncoded
    @POST("user/login")
    Call<LoginResponse> login(@Field("name") String name, @Field("phone") String phone);

    @FormUrlEncoded
    @POST("my/gcm/id/update")
    Call<ErrorResponse> updateFCMId(@Field("gcm_registration_id") String id);

    @GET("my/chats")
    Call<List<Chat>> getMyChats(@Query("token") String token);
}
