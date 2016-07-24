package com.android.privatemessenger.data.api;

import com.android.privatemessenger.data.model.Chat;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface IAPIService {
    @GET("my/chats")
    Call<List<Chat>> getMyChats();
}
