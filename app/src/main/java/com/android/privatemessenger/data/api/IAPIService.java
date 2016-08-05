package com.android.privatemessenger.data.api;

import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.model.ChatCreateResponse;
import com.android.privatemessenger.data.model.Contact;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.android.privatemessenger.data.model.LoginResponse;
import com.android.privatemessenger.data.model.Message;
import com.android.privatemessenger.data.model.SendMessageResponse;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.data.model.UserId;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IAPIService {
    String PARAM_USER_IDS = "user_ids";
    String PARAM_CHAT_NAME = "chat_name";

    @FormUrlEncoded
    @POST("user/login")
    Call<LoginResponse> login(@Field("name") String name, @Field("phone") String phone);

    @FormUrlEncoded
    @POST("user/logout")
    Call<ErrorResponse> logout(@Field("token") String token);

    @FormUrlEncoded
    @POST("my/gcm/id/update")
    Call<ErrorResponse> updateFCMId(@Field("token") String token, @Field("gcm") String id);

    @GET("my/chats")
    Call<List<Chat>> getMyChats(@Query("token") String token);

    @GET("chat/{id}/messages")
    Call<List<Message>> getChatMessages(@Path("id") int chatId,
                                        @Query("token") String token, @Query("limit") int limit, @Query("offset") int offset);

    @GET("contacts")
    Call<List<User>> getRegisteredUsers(@Query("token") String token);

    @GET("chat/{id}/on_message")
    Call<SendMessageResponse> sendMessage(@Path("id") int chatId,
                                          @Query("token") String token, @Query("message") String message);

    @POST("chat/create")
    Call<Chat> createChat(@Body HashMap<String, Object> data);

    @FormUrlEncoded
    @POST("my/profile/update")
    Call<User> updateMyInfo(@Field("token") String token, @Field("name") String name, @Field("email") String email);

    @FormUrlEncoded
    @POST("chat/{id}/delete")
    Call<ErrorResponse> deleteChat(@Path("id") int chatId,
                                   @Field("token") String token);

    @GET("chat/{id}/users")
    Call<List<User>> getChatParticipants(@Path("id") int chatId);

    @GET("my/contacts")
    Call<List<Contact>> getMyContacts(@Query("token") String token);

    @FormUrlEncoded
    @POST("my/contacts/add")
    Call<ErrorResponse> addContact(@Field("token") String token, @Field("name") String name, @Field("phone") String phone);

    @FormUrlEncoded
    @POST("my/contacts/{id}/update")
    Call<ErrorResponse> updateContact(@Path("id") int contactId,
                                      @Field("token") String token, @Field("name") String name, @Field("phone") String phone);

    @GET("my/contacts/{id}/delete")
    Call<ErrorResponse> deleteContact(@Path("id")int chatId, @Field("token") String token);
}