package com.android.privatemessenger.data.api;

import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.android.privatemessenger.data.model.LoginResponse;
import com.android.privatemessenger.data.model.Message;
import com.android.privatemessenger.data.model.SendMessageResponse;
import com.android.privatemessenger.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IAPIService {
    @FormUrlEncoded
    @POST("user/login")
    Call<LoginResponse> login(@Field("name") String name, @Field("phone") String phone);

    @GET("my/gcm/id/update")
    Call<ErrorResponse> updateFCMId(@Query("token") String token, @Query("gcm") String id);

    @GET("my/chats")
    Call<List<Chat>> getMyChats(@Query("token") String token);

    @GET("chat/{id}/messages")
    Call<List<Message>> getChatMessages(@Path("id") int chatId, @Query("token") String token);

    @GET("contacts")
    Call<List<User>> getContacts(@Query("token") String token);

    @GET("chat/{id}/on_message")
    Call<SendMessageResponse> sendMessage(@Path("id") int chatId, @Query("token") String token, @Query("message") String message);
}