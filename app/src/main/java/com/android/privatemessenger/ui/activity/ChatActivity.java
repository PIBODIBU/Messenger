package com.android.privatemessenger.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.android.privatemessenger.R;
import com.android.privatemessenger.broadcast.IntentFilters;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.android.privatemessenger.data.model.Message;
import com.android.privatemessenger.data.model.SendMessageResponse;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.android.privatemessenger.ui.adapter.ChatAdapter;
import com.android.privatemessenger.ui.adapter.RecyclerItemClickListener;
import com.android.privatemessenger.utils.IntentKeys;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseNavDrawerActivity {

    private final String TAG = ChatActivity.this.getClass().getSimpleName();

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    @BindView(R.id.et_message)
    public EditText ETMessage;

    private ChatAdapter adapter;
    private ArrayList<Message> dataSet;
    private LinearLayoutManager linearLayoutManager;

    private Chat chat;

    private BroadcastReceiver messageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (getIntent() != null) {
            chat = (Chat) getIntent().getSerializableExtra(IntentKeys.OBJECT_CHAT);
        }

        ButterKnife.bind(this);
        getDrawer();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(chat.getParticipantsCount() == 2 ? chat.getParticipants().get(0).getName() : chat.getName());
        }

        setupRecyclerView();
        setupReceivers();
        loadData();
    }

    @OnClick(R.id.btn_send)
    public void sendMessage() {
        String message = ETMessage.getText().toString();

        if (message.equals("")) {
            return;
        }

        RetrofitAPI.getInstance().sendMessage(
                chat.getId(),
                SharedPrefUtils.getInstance(this).getUser().getToken(),
                message).enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                if (response.body() != null) {
                    dataSet.add(response.body().getMessage());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {

            }
        });
    }

    private void loadData() {
        RetrofitAPI.getInstance().getChatMessages(chat.getId(), SharedPrefUtils.getInstance(this).getUser().getToken()).enqueue(new Callback<List<Message>>() {
            private void onError() {
                Toast.makeText(ChatActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response == null || response.body() == null) {
                    onError();
                }

                assert response != null;
                for (Message message : response.body()) {
                    dataSet.add(message);
                }

                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(1);
                Log.d(TAG, "onResponse()-> Message: " + dataSet.get(1).getMessage());
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Log.e(TAG, "onFailure()-> Cannot load messages", t);
                onError();
            }
        });
    }

    private void setupRecyclerView() {
        dataSet = new ArrayList<>();
        adapter = new ChatAdapter(this, dataSet);
        linearLayoutManager = new LinearLayoutManager(this);

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        adapter.setRecyclerItemClickListener(new RecyclerItemClickListener() {
            @Override
            public void onClick(int position) {

            }

            @Override
            public void onLongClick(int position) {

            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void setupReceivers() {
        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
               /* Message message = new Message(
                        intent.getIntExtra()
                );*/
            }
        };
        registerReceiver(messageReceiver, new IntentFilter(IntentFilters.NEW_MESSAGE));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(messageReceiver);
        super.onDestroy();
    }
}
